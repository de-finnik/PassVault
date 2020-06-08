import de.finnik.AES.RealRandom;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RealRandomTest {
    @Test
    public void testGetSeedFromInput() {
        String input = "<yxcvbnm,.-#äölkjhgfdsaqwertzuiopü+0987654321>";
        List<String> in = new ArrayList<>(Arrays.asList(input.split("")));
        RealRandom random = new RealRandom();
        for (int i = 0; i < 500; i++) {
            Collections.shuffle(in);
            random.getSeedFromInput(in.stream().limit(16).collect(Collectors.joining()));
        }
    }
}
