import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Player {
    private String first;
    private String last;
    private java.util.List<Integer> goals;
    private java.util.List<Integer> assists;
    private java.util.List<Integer> gp;
    private String born;





}
