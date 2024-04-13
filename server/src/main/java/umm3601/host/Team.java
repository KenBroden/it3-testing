package umm3601.host;

import java.util.List;

import org.mongojack.Id;
import org.mongojack.ObjectId;

public class Team {
  @ObjectId @Id
  public String _id;
  public String teamName;
  public List<String> teamMembers;

  public void addMember(String hunterName) {
    this.teamMembers.add(hunterName);
  }

  public void removeMember(String hunterName) {
    this.teamMembers.remove(hunterName);
  }

}
