package umm3601.host;

import java.util.List;

import org.mongojack.Id;
import org.mongojack.ObjectId;

@SuppressWarnings({"VisibilityModifier"})
public class Host {

    @ObjectId @Id
    @SuppressWarnings({"MemberName"})
    public String _id;

    public String name;
    public String userName;
    public String email;
    public List<Team> teams;

}
