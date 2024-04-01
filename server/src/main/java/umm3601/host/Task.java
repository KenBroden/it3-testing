package umm3601.host;

import java.util.ArrayList;

import org.mongojack.Id;
import org.mongojack.ObjectId;

@SuppressWarnings({"VisibilityModifier"})
public class Task {

    @ObjectId @Id
    @SuppressWarnings({"MemberName"})
    public String _id;

    public String huntId;

    public String name;
    public boolean status;
    public ArrayList<String> photos;
}
