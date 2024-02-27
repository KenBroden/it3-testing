package umm3601.host;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Controller;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;

public class HostController implements Controller {

  private static final String API_HOSTS = "/api/hosts";
  private static final String API_HOST_BY_ID = "/api/hosts/{id}";
  private static final String API_HUNTS = "/api/hunts";
  private static final String API_TASKS = "/api/hunts";

  static final String HOST_KEY = "hostId";
  static final String HUNT_KEY = "huntId";

  static final int REASONABLE_NAME_LENGTH_HUNT = 50;
  static final int REASONABLE_DESCRIPTION_LENGTH_HUNT = 200;
  private static final int REASONABLE_EST_LENGTH_HUNT = 240;

  static final int REASONABLE_NAME_LENGTH_TASK = 150;

  private final JacksonMongoCollection<Host> hostCollection;
  private final JacksonMongoCollection<Hunt> huntCollection;
  private final JacksonMongoCollection<Task> taskCollection;

  public HostController(MongoDatabase database) {
    hostCollection = JacksonMongoCollection.builder().build(
      database,
      "hosts",
      Host.class,
       UuidRepresentation.STANDARD);
    huntCollection = JacksonMongoCollection.builder().build(
      database,
      "hunts",
      Hunt.class,
       UuidRepresentation.STANDARD);
    taskCollection = JacksonMongoCollection.builder().build(
      database,
      "tasks",
      Task.class,
       UuidRepresentation.STANDARD);
  }

  public void getHunt(Context ctx) {
    String id = ctx.pathParam("id");
    Hunt hunt;

    try {
      hunt = huntCollection.find(eq("_id", new ObjectId(id))).first();
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("The requested hunt id wasn't a legal Mongo Object ID.");
    }
    if (hunt == null) {
      throw new NotFoundResponse("The requested hunt was not found");
    } else {
      ctx.json(hunt);
      ctx.status(HttpStatus.OK);
    }
  }

  public void getHunts(Context ctx) {
    Bson combinedFilter = constructFilterHunts(ctx);
    Bson sortingOrder = constructSortingOrderHunts(ctx);

    ArrayList<Hunt> matchingHunts = huntCollection
      .find(combinedFilter)
      .sort(sortingOrder)
      .into(new ArrayList<>());

    ctx.json(matchingHunts);

    ctx.status(HttpStatus.OK);
  }

  private Bson constructFilterHunts(Context ctx) {
    List<Bson> filters = new ArrayList<>();

    if (ctx.queryParamMap().containsKey(HOST_KEY)) {
      String targetHost = ctx.queryParamAsClass(HOST_KEY, String.class).get();
      filters.add(eq(HOST_KEY, targetHost));
    }

    Bson combinedFilter = filters.isEmpty() ? new Document() : and(filters);

    return combinedFilter;
  }

  private Bson constructSortingOrderHunts(Context ctx) {
    String sortBy = Objects.requireNonNullElse(ctx.queryParam("sortby"), "name");
    Bson sortingOrder = Sorts.ascending(sortBy);
    return sortingOrder;
  }

  public void getTasks(Context ctx) {
    Bson combinedFilter = constructFilterTasks(ctx);
    Bson sortingOrder = constructSortingOrderTasks(ctx);

    ArrayList<Task> matchingTasks = taskCollection
      .find(combinedFilter)
      .sort(sortingOrder)
      .into(new ArrayList<>());

    ctx.json(matchingTasks);

    ctx.status(HttpStatus.OK);
  }

  private Bson constructFilterTasks(Context ctx) {
    List<Bson> filters = new ArrayList<>();

    if (ctx.queryParamMap().containsKey(HUNT_KEY)) {
      String targetHunt = ctx.queryParamAsClass(HUNT_KEY, String.class).get();
      filters.add(eq(HUNT_KEY, targetHunt));
    }

    Bson combinedFilter = filters.isEmpty() ? new Document() : and(filters);

    return combinedFilter;
  }

  private Bson constructSortingOrderTasks(Context ctx) {
    String sortBy = Objects.requireNonNullElse(ctx.queryParam("sortby"), "name");
    Bson sortingOrder = Sorts.ascending(sortBy);
    return sortingOrder;
  }

  public void addNewHunt(Context ctx) {
    Hunt newHunt = ctx.bodyValidator(Hunt.class)
    .check(hunt -> hunt.hostId != null && hunt.hostId.length() > 0, "Invalid hostId")
    .check(hunt -> hunt.name.length() < REASONABLE_NAME_LENGTH_HUNT, "Name must be less than 50 characters")
    .check(hunt -> hunt.name.length() > 0, "Name must be at least 1 character")
    .check(hunt -> hunt.description.length() < REASONABLE_DESCRIPTION_LENGTH_HUNT,
     "Description must be less than 200 characters")
    .check(hunt -> hunt.est < REASONABLE_EST_LENGTH_HUNT, "Estimated time must be less than 4 hours")
    .get();

    huntCollection.insertOne(newHunt);
    ctx.json(Map.of("id", newHunt._id));
    ctx.status(HttpStatus.CREATED);
  }

  public void addNewTask(Context ctx) {
    Task newTask = ctx.bodyValidator(Task.class)
    .check(task -> task.huntId != null && task.huntId.length() > 0, "Invalid huntId")
    .check(task -> task.name.length() < REASONABLE_NAME_LENGTH_TASK, "Name must be less than 150 characters")
    .check(task -> task.name.length() > 0, "Name must be at least 1 character")
    .get();

    taskCollection.insertOne(newTask);
    ctx.json(Map.of("id", newTask._id));
    ctx.status(HttpStatus.CREATED);
  }

  @Override
  public void addRoutes(Javalin server) {

    server.get(API_HUNTS, this::getHunts);

    server.post(API_HUNTS, this::addNewHunt);

    server.get(API_TASKS, this::getTasks);

    server.post(API_TASKS, this::addNewTask);
  }

}
