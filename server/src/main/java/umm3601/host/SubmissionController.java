package umm3601.host;

import org.bson.UuidRepresentation;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import static com.mongodb.client.model.Filters.eq;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import umm3601.Controller;

public class SubmissionController implements Controller{

  private static final String API_SUBMISSION = "/api/submissions/{id}";
  //private static final String API_SUBMISSIONS = "/api/submissions";
  //private static final String API_CREATE_SUBMISSIONS = "/api/submissions/create";
  private static final String API_SUBMISSIONS_BY_TEAM = "/api/submissions/team/{teamId}";
  private static final String API_SUBMISSIONS_BY_TASK = "/api/submissions/task/{taskId}";
  private static final String API_SUBMISSIONS_BY_TEAM_AND_TASK = "/api/submissions/team/{teamId}/task/{taskId}";
  private static final String API_SUBMISSIONS_BY_STARTEDHUNT = "/api/submissions/startedHunt/{startedHuntId}";
  private static final String API_SUBMISSION_GET_PHOTO = "/api/submissions/{id}/photo";

  private final JacksonMongoCollection<Submission> submissionCollection;

  public SubmissionController(MongoDatabase database) {

    submissionCollection = JacksonMongoCollection.builder().build(
        database,
        "submissions",
        Submission.class,
        UuidRepresentation.STANDARD);
  }

  public Submission getSubmission(Context ctx) {
    String id = ctx.pathParam("id");

    return submissionCollection.find(eq("_id", new ObjectId(id))).first();
  }

  public void getSubmissionsByTeam(Context ctx) {
    String teamId = ctx.pathParam("teamId");

    List<Submission> teamSubmissions = submissionCollection.find(eq("teamId", teamId)).into(new ArrayList<>());
    ctx.json(teamSubmissions);
    ctx.status(HttpStatus.OK);
  }

  public Submission getSubmissionsByTask(Context ctx) {
    String taskId = ctx.pathParam("taskId");

    return submissionCollection.find(eq("taskId", taskId)).first();
  }

  public Submission getSubmissionsByTeamAndTask(Context ctx) {
    String teamId = ctx.pathParam("teamId");
    String taskId = ctx.pathParam("taskId");

    return submissionCollection.find(eq("teamId", teamId)).filter(eq("taskId", taskId)).first();
  }

  public Submission getSubmissionsByStartedHunt(Context ctx) {
    String startedHuntId = ctx.pathParam("startedHuntId");

    return submissionCollection.find(eq("startedHuntId", startedHuntId)).first();
  }

  public String getPhotoFromSubmission(Context ctx) {
    String submissionId = ctx.pathParam("id");
    Submission submission = submissionCollection.find(eq("_id", new ObjectId(submissionId))).first();

    File photo = new File("photos/" + submission.photoPath);
    if (photo.exists()) {
      try {
        byte[] bytes = Files.readAllBytes(Paths.get(photo.getPath()));
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return "";
  }

  @Override
  public void addRoutes(Javalin server) {
    server.get(API_SUBMISSION, this::getSubmission);
    server.get(API_SUBMISSIONS_BY_TEAM, this::getSubmissionsByTeam);
    server.get(API_SUBMISSIONS_BY_TASK, this::getSubmissionsByTask);
    server.get(API_SUBMISSIONS_BY_TEAM_AND_TASK, this::getSubmissionsByTeamAndTask);
    server.get(API_SUBMISSIONS_BY_STARTEDHUNT, this::getSubmissionsByStartedHunt);
    server.get(API_SUBMISSION_GET_PHOTO, this::getPhotoFromSubmission);
  }



}
