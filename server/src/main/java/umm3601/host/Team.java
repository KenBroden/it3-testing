package umm3601.host;

import org.mongojack.Id;
import org.mongojack.ObjectId;

@SuppressWarnings({"VisibilityModifier"})
public class Team {
  @ObjectId @Id
  @SuppressWarnings({"MemberName"})
  public String _id;
  public String teamName;
  public String startedHuntId;

}
