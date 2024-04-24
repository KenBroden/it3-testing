package umm3601.team;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.json.JavalinJackson;
import io.javalin.validation.BodyValidator;
import io.javalin.validation.ValidationException;
import io.javalin.http.Context;

import umm3601.host.CompleteHunt;
import umm3601.host.EndedHunt;
import umm3601.host.Host;
import umm3601.host.Hunt;
import umm3601.host.StartedHunt;
import umm3601.host.Task;
import umm3601.host.Team;
import umm3601.host.TeamController;

@SuppressWarnings({ "MagicNumber" })
public class TeamControllerSpec {
  private TeamController teamController;
  private ObjectId frysId;
  private ObjectId huntId;
  private ObjectId taskId;
  private ObjectId startedHuntId;
  private ObjectId teamId;

  private static MongoClient mongoClient;
  private static MongoDatabase db;
  private static JavalinJackson javalinJackson = new JavalinJackson();

  @Mock
  private Context ctx;

  @Captor
  private ArgumentCaptor<ArrayList<Hunt>> huntArrayListCaptor;

  @Captor
  private ArgumentCaptor<ArrayList<Task>> taskArrayListCaptor;

  @Captor
  private ArgumentCaptor<Host> hostCaptor;

  @Captor
  private ArgumentCaptor<CompleteHunt> completeHuntCaptor;

  @Captor
  private ArgumentCaptor<StartedHunt> startedHuntCaptor;

  @Captor
  private ArgumentCaptor<ArrayList<StartedHunt>> startedHuntArrayListCaptor;

  @Captor
  private ArgumentCaptor<EndedHunt> finishedHuntCaptor;

  @Captor
  private ArgumentCaptor<Map<String, String>> mapCaptor;

  @Captor
  private ArgumentCaptor<Team> teamCaptor;

  @Captor
  private ArgumentCaptor<ArrayList<Team>> teamArrayListCaptor;

  @BeforeAll
  static void setupAll() {
    String mongoAddr = System.getenv().getOrDefault("MONGO_ADDR", "localhost");

    mongoClient = MongoClients.create(
        MongoClientSettings.builder()
            .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(mongoAddr))))
            .build());
    db = mongoClient.getDatabase("test");
  }

  @AfterAll
  static void teardown() {
    db.drop();
    mongoClient.close();
  }

  @BeforeEach
  void setupEach() throws IOException {
    MockitoAnnotations.openMocks(this);

    setupHosts();
    setupHunts();
    setupTasks();
    setupTeams();
    setupSubmissions();
    setupStartedHunts();

    teamController = new TeamController(db);
  }

  void setupHosts() {
    MongoCollection<Document> hostDocuments = db.getCollection("hosts");
    hostDocuments.drop();
    frysId = new ObjectId();
    Document fry = new Document()
        .append("_id", frysId)
        .append("name", "Fry")
        .append("userName", "fry")
        .append("email", "fry@email");

    hostDocuments.insertOne(fry);
  }

  private List<Document> testHunts;

  void setupHunts() {
    MongoCollection<Document> huntDocuments = db.getCollection("hunts");
    huntDocuments.drop();
    testHunts = new ArrayList<>();
    testHunts.add(
        new Document()
            .append("hostId", "frysId")
            .append("name", "Fry's Hunt")
            .append("description", "Fry's hunt for the seven leaf clover")
            .append("est", 20)
            .append("numberOfTasks", 5));
    testHunts.add(
        new Document()
            .append("hostId", "frysId")
            .append("name", "Fry's Hunt 2")
            .append("description", "Fry's hunt for Morris")
            .append("est", 30)
            .append("numberOfTasks", 2));
    testHunts.add(
        new Document()
            .append("hostId", "frysId")
            .append("name", "Fry's Hunt 3")
            .append("description", "Fry's hunt for money")
            .append("est", 40)
            .append("numberOfTasks", 1));
    testHunts.add(
        new Document()
            .append("hostId", "differentId")
            .append("name", "Different's Hunt")
            .append("description", "Different's hunt for money")
            .append("est", 60)
            .append("numberOfTasks", 10));

    huntId = new ObjectId();
    Document hunt = new Document()
        .append("_id", huntId)
        .append("hostId", "frysId")
        .append("name", "Best Hunt")
        .append("description", "This is the best hunt")
        .append("est", 20)
        .append("numberOfTasks", 3);

    huntDocuments.insertMany(testHunts);
    huntDocuments.insertOne(hunt);
  }

  private List<Document> testTasks;

  void setupTasks() {
    MongoCollection<Document> taskDocuments = db.getCollection("tasks");
    taskDocuments.drop();
    testTasks = new ArrayList<>();
    testTasks.add(
        new Document()
            .append("huntId", huntId.toHexString())
            .append("name", "Take a picture of a cat")
            .append("status", false)
            .append("photos", new ArrayList<String>()));
    testTasks.add(
        new Document()
            .append("huntId", huntId.toHexString())
            .append("name", "Take a picture of a dog")
            .append("status", false)
            .append("photos", new ArrayList<String>()));
    testTasks.add(
        new Document()
            .append("huntId", huntId.toHexString())
            .append("name", "Take a picture of a park")
            .append("status", true)
            .append("photos", new ArrayList<String>()));
    testTasks.add(
        new Document()
            .append("huntId", "differentId")
            .append("name", "Take a picture of a moose")
            .append("status", true)
            .append("photos", new ArrayList<String>()));

    taskId = new ObjectId();
    Document task = new Document()
        .append("_id", taskId)
        .append("huntId", "someId")
        .append("name", "Best Task")
        .append("status", false)
        .append("photos", new ArrayList<String>());

    taskDocuments.insertMany(testTasks);
    taskDocuments.insertOne(task);
  }

  private List<Document> testTeams;

  protected void setupTeams() {
    MongoCollection<Document> teamDocuments = db.getCollection("teams");
    teamDocuments.drop();
    testTeams = new ArrayList<>();
    testTeams.add(
        new Document()
            .append("teamName", "Team 1"));
    testTeams.add(
        new Document()
            .append("teamName", "Team 2"));
    testTeams.add(
        new Document()
            .append("teamName", "Team 3"));

    teamId = new ObjectId();
    Document team = new Document()
        .append("_id", teamId)
        .append("teamName", "Team 4");

    teamDocuments.insertMany(testTeams);
    teamDocuments.insertOne(team);
  }

  private List<Document> testSubmissions;

  protected void setupSubmissions() {
    MongoCollection<Document> submissionDocuments = db.getCollection("submissions");
    submissionDocuments.drop();
    testSubmissions = new ArrayList<>();
    testSubmissions.add(
        new Document()
            .append("taskId", "Task 1")
            .append("teamId", "Team 1")
            .append("photoPath", "Path 1")
            .append("submitTime", new Date()));
    testSubmissions.add(
        new Document()
            .append("taskId", "Task 2")
            .append("teamId", "Team 2")
            .append("photoPath", "Path 2")
            .append("submitTime", new Date()));
    testSubmissions.add(
        new Document()
            .append("taskId", "Task 3")
            .append("teamId", "Team 3")
            .append("photoPath", "Path 3")
            .append("submitTime", new Date()));

    ObjectId submissionId = new ObjectId();
    Document submission = new Document()
        .append("_id", submissionId)
        .append("taskId", "Task 4")
        .append("teamId", "Team 4")
        .append("photoPath", "Path 4")
        .append("submitTime", new Date());

    submissionDocuments.insertMany(testSubmissions);
    submissionDocuments.insertOne(submission);
  }

  protected void setupStartedHunts() {
    MongoCollection<Document> startedHuntsDocuments = db.getCollection("startedHunts");
    startedHuntsDocuments.drop();
    List<Document> startedHunts = new ArrayList<>();
    Calendar calendar = Calendar.getInstance();
    calendar.set(2024, Calendar.MAY, 2, 12, 0, 0);
    Date date = calendar.getTime();
    startedHunts.add(
        new Document()
            .append("accessCode", "123456")
            .append("completeHunt", new Document()
                .append("hunt", testHunts.get(0))
                .append("tasks", testTasks.subList(0, 2)))
            .append("status", true)
            .append("endDate", null)
            .append("submissionIds", testSubmissions.subList(0, 2)));

    startedHunts.add(
        new Document()
            .append("accessCode", "654321")
            .append("completeHunt", new Document()
                .append("hunt", testHunts.get(1))
                .append("tasks", testTasks.subList(2, 3)))
            .append("status", false)
            .append("endDate", date)
            .append("teams", testSubmissions.subList(2, 3)));

    startedHunts.add(
        new Document()
            .append("accessCode", "123459")
            .append("completeHunt", new Document()
                .append("hunt", testHunts.get(2))
                .append("tasks", testTasks.subList(0, 3)))
            .append("status", true)
            .append("endDate", null)
            .append("teams", testSubmissions.subList(0, 3)));

    startedHuntId = new ObjectId();
    Document startedHunt = new Document()
        .append("_id", startedHuntId)
        .append("accessCode", "123456")
        .append("completeHunt", new Document()
            .append("hunt", testHunts.get(2))
            .append("tasks", testTasks.subList(0, 3)))
        .append("status", true)
        .append("endDate", null)
        .append("teams", testSubmissions.subList(0, 3));

    startedHuntsDocuments.insertMany(startedHunts);
    startedHuntsDocuments.insertOne(startedHunt);

    teamController = new TeamController(db);
  }

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

}
