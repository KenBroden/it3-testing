package umm3601.host;

import java.util.List;

import org.mongojack.Id;
import org.mongojack.ObjectId;

@SuppressWarnings({"VisibilityModifier"})
public class HuntTeams {
  @ObjectId @Id
  @SuppressWarnings({"MemberName"})
  public String _id;
  public String startedHuntid;
  public List<String> teamIds;

  public void addTeam(String teamId) {
    this.teamIds.add(teamId);
  }

  public void removeTeam(String teamId) {
    this.teamIds.remove(teamId);
  }

  public void setStartedHuntid(String startedHuntid) {
    this.startedHuntid = startedHuntid;
  }

  public String getStartedHuntid() {
    return startedHuntid;
  }

  public String getTeamid(int index) {
    return teamIds.get(index);
  }
}
