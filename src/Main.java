import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.StudentsGroup;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] argv) throws Exception {
        File fileIn = new File(argv[0]);
        File fileOut = new File(argv[1]);

        ObjectMapper objectMapper = new ObjectMapper();

        List<StudentsGroup> groups = objectMapper.readValue(fileIn, new TypeReference<List<StudentsGroup>>(){});
        objectMapper.writeValue(fileOut, groups);
    }
}
