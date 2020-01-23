package gov.ismonnet.cardhelp.core;

import java.util.List;

public interface GamesDeserializer {

    List<String> deserialize(String toDeserialize) throws SerializationException;
}
