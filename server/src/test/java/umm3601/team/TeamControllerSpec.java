package umm3601.team;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.validation.BodyValidator;
import io.javalin.validation.ValidationException;
import umm3601.TestSetup;

import umm3601.host.Team;

@SuppressWarnings({ "MagicNumber" })
public class TeamControllerSpec extends TestSetup{

  @Test
  void addRoutes() {
    Javalin mockServer = mock(Javalin.class);
    teamController.addRoutes(mockServer);
    verify(mockServer, Mockito.atLeast(1)).get(any(), any());
  }

  @Test
  void getTeamById() throws IOException {
    String id = teamId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);

    Team team = teamController.getTeam(ctx);

    assertEquals("Team 4", team.teamName);
    assertEquals(0, team.teamMembers.size());
  }

  @Test
  void getTeamWithBadId() throws IOException {
    when(ctx.pathParam("id")).thenReturn("badId");

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      teamController.getTeam(ctx);
    });

    assertEquals("The requested team id wasn't a legal Mongo Object ID.", exception.getMessage());
  }

  @Test
  void getTeamWithNonExistentId() throws IOException {
    when(ctx.pathParam("id")).thenReturn(new ObjectId().toHexString());

    Throwable exception = assertThrows(NotFoundResponse.class, () -> {
      teamController.getTeam(ctx);
    });

    assertEquals("The requested team was not found", exception.getMessage());
  }

  @Test
  void createTeam() throws IOException {
    String testNewTeam = """
        {
          "teamName": "Team 5",
          "teamMembers": ["fry", "barb", "tom"]
        }
        """;
    when(ctx.bodyValidator(Team.class))
        .then(value -> new BodyValidator<Team>(testNewTeam, Team.class, javalinJackson));

    teamController.createTeam(ctx);
    verify(ctx).json(mapCaptor.capture());

    verify(ctx).status(HttpStatus.CREATED);

    Document createdTeam = db.getCollection("teams")
        .find(eq("_id", new ObjectId(mapCaptor.getValue().get("id")))).first();

    assertNotNull(createdTeam);
    assertEquals("Team 5", createdTeam.getString("teamName"));
    assertEquals(3, createdTeam.getList("teamMembers", String.class).size());
  }

  @Test
  void createTeamWithNullName() {
    String testNewTeam = """
        {
          "teamName": null,
          "teamMembers": ["fry", "barb", "tom"]
        }
        """;
    when(ctx.bodyValidator(Team.class))
        .then(value -> new BodyValidator<Team>(testNewTeam, Team.class, javalinJackson));

    assertThrows(ValidationException.class, () -> teamController.createTeam(ctx));
  }

  @Test
  void createTeamWithEmptyName() {
    String testNewTeam = """
        {
          "teamName": "",
          "teamMembers": ["fry", "barb", "tom"]
        }
        """;
    when(ctx.bodyValidator(Team.class))
        .then(value -> new BodyValidator<Team>(testNewTeam, Team.class, javalinJackson));

    assertThrows(ValidationException.class, () -> teamController.createTeam(ctx));
  }

  @Test
  void getTeams() throws IOException {
    teamController.getTeams(ctx);
    verify(ctx).json(any());
    verify(ctx).status(HttpStatus.OK);
  }

  @Test
  void deleteTeam() throws IOException {
    String id = teamId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);

    teamController.deleteTeam(ctx);
    verify(ctx).status(HttpStatus.OK);

    Document deletedTeam = db.getCollection("teams")
        .find(eq("_id", new ObjectId(id))).first();

    assertNull(deletedTeam);
  }

  @Test
  void deleteTeamWithBadId() throws IOException {
    when(ctx.pathParam("id")).thenReturn("badId");

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      teamController.deleteTeam(ctx);
    });

    assertEquals("The requested team id wasn't a legal Mongo Object ID.", exception.getMessage());
  }

  @Test
  void deleteTeamWithNonExistentId() throws IOException {
    ObjectId id = new ObjectId();
    when(ctx.pathParam("id")).thenReturn(id.toHexString());

    Throwable exception = assertThrows(NotFoundResponse.class, () -> {
      teamController.deleteTeam(ctx);
    });

    assertEquals("Was unable to delete team with id: " + id.toHexString()
        + "; perhaps its an illegal id, or the id is not in the database.", exception.getMessage());
  }

  @Test
  void joinTeam() throws IOException {
    String id = teamId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);
    when(ctx.queryParam("name")).thenReturn("fry");

    teamController.joinTeam(ctx);
    verify(ctx).status(HttpStatus.OK);

    Document updatedTeam = db.getCollection("teams")
        .find(eq("_id", new ObjectId(id))).first();

    assertNotNull(updatedTeam);
    assertEquals("Team 4", updatedTeam.getString("teamName"));
    assertEquals(1, updatedTeam.getList("teamMembers", String.class).size());
    assertTrue(updatedTeam.getList("teamMembers", String.class).contains("fry"));
  }

  @Test
  void joinTeamWithBadId() throws IOException {
    when(ctx.pathParam("id")).thenReturn("badId");
    when(ctx.queryParam("name")).thenReturn("fry");

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      teamController.joinTeam(ctx);
    });

    assertEquals("The requested team id wasn't a legal Mongo Object ID.", exception.getMessage());
  }

  @Test
  void joinTeamWithNonExistentId() throws IOException {
    ObjectId id = new ObjectId();
    when(ctx.pathParam("id")).thenReturn(id.toHexString());
    when(ctx.queryParam("name")).thenReturn("fry");

    Throwable exception = assertThrows(NotFoundResponse.class, () -> {
      teamController.joinTeam(ctx);
    });

    assertEquals("The requested team was not found", exception.getMessage());
  }

  @Test
  void joinTeamWithNullName() {
    String id = teamId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);
    when(ctx.queryParam("name")).thenReturn(null);

    assertThrows(BadRequestResponse.class, () -> teamController.joinTeam(ctx));
  }

  @Test
  void joinTeamWithEmptyName() {
    String id = teamId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);
    when(ctx.queryParam("name")).thenReturn("");

    assertThrows(BadRequestResponse.class, () -> teamController.joinTeam(ctx));
  }

  @Test
  void leaveTeam() throws IOException {
    String id = teamId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);
    when(ctx.queryParam("name")).thenReturn("fry");

    teamController.leaveTeam(ctx);
    verify(ctx).status(HttpStatus.OK);

    Document updatedTeam = db.getCollection("teams")
        .find(eq("_id", new ObjectId(id))).first();

    assertNotNull(updatedTeam);
    assertEquals("Team 4", updatedTeam.getString("teamName"));
    assertEquals(0, updatedTeam.getList("teamMembers", String.class).size());
  }

  @Test
  void leaveTeamWithBadId() throws IOException {
    when(ctx.pathParam("id")).thenReturn("badId");
    when(ctx.queryParam("name")).thenReturn("fry");

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      teamController.leaveTeam(ctx);
    });

    assertEquals("The requested team id wasn't a legal Mongo Object ID.", exception.getMessage());
  }

  @Test
  void leaveTeamWithNonExistentId() throws IOException {
    ObjectId id = new ObjectId();
    when(ctx.pathParam("id")).thenReturn(id.toHexString());
    when(ctx.queryParam("name")).thenReturn("fry");

    Throwable exception = assertThrows(NotFoundResponse.class, () -> {
      teamController.leaveTeam(ctx);
    });

    assertEquals("The requested team was not found", exception.getMessage());
  }

  @Test
  void leaveTeamWithNullName() {
    String id = teamId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);
    when(ctx.queryParam("name")).thenReturn(null);

    assertThrows(BadRequestResponse.class, () -> teamController.leaveTeam(ctx));
  }

  @Test
  void leaveTeamWithEmptyName() {
    String id = teamId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);
    when(ctx.queryParam("name")).thenReturn("");

    assertThrows(BadRequestResponse.class, () -> teamController.leaveTeam(ctx));
  }

}
