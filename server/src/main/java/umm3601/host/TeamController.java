package umm3601.host;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.UuidRepresentation;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Controller;

import static com.mongodb.client.model.Filters.eq;

public class TeamController implements Controller {

  private static final String API_TEAM = "/api/teams/{id}";
  private static final String API_TEAMS = "/api/teams";
  private static final String API_CREATE_TEAMS = "/api/teams/create";

  private final JacksonMongoCollection<Team> teamCollection;

  public TeamController(MongoDatabase database) {

    teamCollection = JacksonMongoCollection.builder().build(
        database,
        "teams",
        Team.class,
        UuidRepresentation.STANDARD);
  }

  public void createTeam(Context ctx) {
    Team newTeam = ctx.bodyValidator(Team.class)
        .check(team -> team.teamName != null && team.teamName.length() > 0, "Team name is required")
        .get();

    teamCollection.insertOne(newTeam);
    ctx.json(Map.of("id", newTeam._id));
    ctx.status(HttpStatus.CREATED);
  }

  public Team getTeam(Context ctx) {
    String id = ctx.pathParam("id");
    Team team;

    try {
      team = teamCollection.find(eq("_id", new ObjectId(id))).first();
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("The requested team id wasn't a legal Mongo Object ID.");
    }
    if (team == null) {
      throw new NotFoundResponse("The requested team was not found");
    } else {
      return team;
    }
  }

  public void getTeams(Context ctx) {
    List<Team> allTeams = teamCollection.find().into(new ArrayList<>());
    ctx.json(allTeams);
    ctx.status(HttpStatus.OK);
  }

  public void deleteTeam(Context ctx) {
    String id = ctx.pathParam("id");
    try {
      DeleteResult teamDeleteResult = teamCollection.deleteOne(eq("_id", new ObjectId(id)));
      if (teamDeleteResult.getDeletedCount() != 1) {
        ctx.status(HttpStatus.NOT_FOUND);
        throw new NotFoundResponse(
            "Was unable to delete team with id: "
                + id
                + "; perhaps its an illegal id, or the id is not in the database.");
      }
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.BAD_REQUEST);
      throw new BadRequestResponse("The requested team id wasn't a legal Mongo Object ID.");
    }
    ctx.status(HttpStatus.OK);
  }

  public void createTeams(Context ctx) {
    String startedHuntId = ctx.queryParam("startedHuntId");
    int numTeams;
    try {
        numTeams = Integer.parseInt(ctx.queryParam("numTeams"));
    } catch (NumberFormatException e) {
        throw new BadRequestResponse("Invalid number of teams specified");
    }

    if (numTeams <= 0) {
        throw new BadRequestResponse("Number of teams must be greater than 0");
    }

    List<Team> teams = new ArrayList<>();
    for (int i = 1; i <= numTeams; i++) {
        Team team = new Team();
        team.teamName = "Team " + i;
        team.startedHuntId = startedHuntId; // Set the startedHuntId to the team
        teams.add(team);
    }

    teamCollection.insertMany(teams);
    ctx.status(HttpStatus.CREATED);
    ctx.json(Map.of("numTeamsCreated", numTeams));
  }

  public List<Team> getAllStartedHuntTeams(String startedHuntId) {
    return teamCollection.find(eq("startedHuntId", startedHuntId)).into(new ArrayList<>());
  }

  @Override
  public void addRoutes(Javalin server) {
    server.post(API_TEAMS, this::createTeam);
    server.get(API_TEAMS, this::getTeams);
    server.get(API_TEAM, this::getTeam);
    server.delete(API_TEAM, this::deleteTeam);
    server.post(API_CREATE_TEAMS, this::createTeams);
    server.get("/api/startedHunts/{startedHuntId}/teams", ctx -> {
      String startedHuntId = ctx.pathParam("startedHuntId");
      List<Team> teams = getAllStartedHuntTeams(startedHuntId);
      ctx.json(teams);
    });
  }

}
