// package umm3601.hunts;

// import static com.mongodb.client.model.Filters.eq;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.io.IOException;
// import java.util.List;

// import org.bson.Document;
// import org.bson.types.ObjectId;
// import org.junit.jupiter.api.Test;

// import io.javalin.http.BadRequestResponse;
// import io.javalin.http.HttpStatus;
// import io.javalin.http.NotFoundResponse;
// import umm3601.TestSetup;

// @SuppressWarnings({ "MagicNumber", "unchecked" })
// public class StartedHuntControllerSpec extends TestSetup {

//   // Test if teams are added to the started hunt
//   // The method should retrieve a number of teams from the client side host and
//   // add them to the started hunt
//   @Test
//   void addTeamsToStartedHunt() throws IOException {
//     // Prepare the context and the number of teams to add
//     String id = startedHuntId.toHexString();
//     int numTeamsToAdd = 3;
//     when(ctx.pathParam("id")).thenReturn(id);
//     when(ctx.bodyAsClass(Integer.class)).thenReturn(numTeamsToAdd);

//     // Get the number of existing teams
//     Document startedHunt = db.getCollection("startedHunts").find(eq("_id", new ObjectId(id))).first();
//     int numTeamsExisting = ((List<Document>) startedHunt.get("teams")).size();

//     // Run the method to test
//     hostController.addTeamsToStartedHunt(ctx);

//     // Verify the status code
//     verify(ctx).status(HttpStatus.OK);

//     // Retrieve the updated started hunt from the database
//     Document updatedStartedHunt = db.getCollection("startedHunts")
//         .find(eq("_id", new ObjectId(id))).first();

//     // Check if the correct number of teams were added
//     List<Document> teams = (List<Document>) updatedStartedHunt.get("teams");
//     assertEquals(numTeamsExisting + numTeamsToAdd, teams.size());

//     // Check if the teams are empty
//     for (Document team : teams) {
//       List<String> teamMembers = (List<String>) team.get("teamMembers");
//       assertTrue(teamMembers.isEmpty());
//     }
//   }

//   // Test adding teams to a started hunt that can't be found
//   @Test
//   void addTeamsToStartedHuntWithBadId() throws IOException {
//     // Prepare the context with a bad id
//     when(ctx.pathParam("id")).thenReturn("badId");

//     // Run the method to test
//     Throwable exception = assertThrows(BadRequestResponse.class, () -> {
//       hostController.addTeamsToStartedHunt(ctx);
//     });

//     // Verify the error message
//     assertEquals("The requested started hunt id wasn't a legal Mongo Object ID.", exception.getMessage());
//   }

//   // Test removing a team from a started hunt
//   @Test
//   void removeTeamFromStartedHunt() throws IOException {
//       // Setup the test data
//       setupTeams();
//       setupStartedHunts();

//       // Prepare the context and the team id to remove
//       String id = startedHuntId.toHexString();
//       ObjectId teamId = new ObjectId(testTeams.get(0).get("_id").toString()); // Retrieve teamId from test data
//       when(ctx.pathParam("id")).thenReturn(id);
//       when(ctx.pathParam("teamId")).thenReturn(teamId.toHexString()); // Convert ObjectId to String

//       // Get the number of existing teams
//       Document startedHunt = db.getCollection("startedHunts").find(eq("_id", new ObjectId(id))).first();
//       int numTeamsExisting = ((List<Document>) startedHunt.get("teams")).size();

//       // Run the method to test
//       hostController.removeTeamFromStartedHunt(ctx);

//       // Verify the status code
//       verify(ctx).status(HttpStatus.OK);

//       // Retrieve the updated started hunt from the database
//       Document updatedStartedHunt = db.getCollection("startedHunts")
//               .find(eq("_id", new ObjectId(id))).first();

//       // Check if the correct number of teams were removed
//       List<Document> teams = (List<Document>) updatedStartedHunt.get("teams");
//       assertEquals(numTeamsExisting - 1, teams.size());

//       // Check if the correct team was removed
//       boolean teamFound = false;
//       for (Document team : teams) {
//           if (teamId.toHexString().equals(team.get("_id").toString())) { // Use teamId.toHexString()
//               teamFound = true;
//           }
//       }
//       assertFalse(teamFound);
//   }

//   // Test removing a team from a started hunt that can't be found
//   @Test
//   void removeTeamFromStartedHuntWithBadId() throws IOException {
//       // Prepare the context with a bad id
//       when(ctx.pathParam("id")).thenReturn("badId");

//       // Run the method to test
//       Throwable exception = assertThrows(BadRequestResponse.class, () -> {
//           hostController.removeTeamFromStartedHunt(ctx);
//       });

//       // Verify the error message
//       assertEquals("The requested started hunt id wasn't a legal Mongo Object ID.", exception.getMessage());
//   }

//   // Test removing a team that can't be found in the started hunt
//   @Test
//   void removeTeamFromStartedHuntWithNonExistentId() throws IOException {
//       // Setup the test data
//       setupTeams();
//       setupStartedHunts();

//       // Prepare the context with a non-existent team id
//       String id = startedHuntId.toHexString();
//       ObjectId teamId = new ObjectId(); // Create a new ObjectId
//       when(ctx.pathParam("id")).thenReturn(id);
//       when(ctx.pathParam("teamId")).thenReturn(teamId.toHexString()); // Convert ObjectId to String

//       // Run the method to test
//       Throwable exception = assertThrows(NotFoundResponse.class, () -> {
//           hostController.removeTeamFromStartedHunt(ctx);
//       });

//       // Verify the error message
//       assertEquals("Team with ID " + teamId.toHexString() + " does not exist", exception.getMessage());
//   }

// }
