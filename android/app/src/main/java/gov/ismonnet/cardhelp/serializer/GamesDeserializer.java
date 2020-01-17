package gov.ismonnet.cardhelp.serializer;

import java.util.Collection;

public interface GamesDeserializer {

    Collection<String> deserialize(String toDeserialize);
}
