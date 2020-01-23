package gov.ismonnet.cardhelp.core;

public interface DetectionDeserializer {

    Detection deserialize(String string) throws SerializationException;
}
