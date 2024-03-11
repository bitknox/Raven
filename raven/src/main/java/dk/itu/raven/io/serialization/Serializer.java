package dk.itu.raven.io.serialization;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {
	public static void serialize(String path, Object obj) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(path);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		objectOutputStream.writeObject(obj);
		objectOutputStream.flush();
		objectOutputStream.close();

	}

	public static Object deserialize(String path) throws IOException, ClassNotFoundException {
		FileInputStream fileInputStream = new FileInputStream(path);
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		Object obj = objectInputStream.readObject();
		objectInputStream.close();
		return obj;
	}
}