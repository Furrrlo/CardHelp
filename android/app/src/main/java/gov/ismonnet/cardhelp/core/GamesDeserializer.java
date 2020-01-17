package gov.ismonnet.cardhelp.core;

import java.util.Collection;

public interface GamesDeserializer {

    Collection<String> deserialize(String toDeserialize);
}
