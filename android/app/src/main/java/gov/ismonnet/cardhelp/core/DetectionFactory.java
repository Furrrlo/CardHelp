package gov.ismonnet.cardhelp.core;

import java.io.File;
import java.time.Instant;
import java.util.Collection;

public interface DetectionFactory {

    Detection makeDetection(File thumbnailPath,
                            File inputPath,
                            File outputPath,
                            Collection<Card> cards,
                            String game,
                            String score,
                            Instant timestamp);
}
