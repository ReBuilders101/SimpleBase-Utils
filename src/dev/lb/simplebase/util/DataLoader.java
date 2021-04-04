package dev.lb.simplebase.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.IntFunction;

/**
 * Static method to load data from files and jar resources
 */
public final class DataLoader {

	/**
	 * Converts {@link URL} to an NIO {@link Path} object. 
	 * @param url The {@link URL} that should be converted to a path 
	 * @return A {@link Path} that represents the resource.
	 * @throws IOException When the resource could not be converted to a {@link Path}.
	 */
	public static Path url2nio(URL url) throws IOException {
		try {
			return Paths.get(url.toURI());
		} catch (URISyntaxException e) {
			throw new IOException("Could not convert JAR resource name to path", e);
		}
	}
	
	/**
	 * Loads a sequence of bytes from a file into a {@link ByteBuffer}.
	 * @param path The {@link Path} to the file
	 * @param allocator A function that creates {@link ByteBuffer}s with the requested capacity
	 * @return The {@link ByteBuffer} with the loaded data. The buffers properties are determined by the allocator function.
	 * @throws IOException When the file is not found or when a channel to read the file cannot be opened.
	 */
	public static ByteBuffer loadBytes(Path path, IntFunction<ByteBuffer> allocator) throws IOException {
		final var fileSize = Files.size(path);
		if(fileSize > Integer.MAX_VALUE)
			throw new OutOfMemoryError("Cannot read files larger than Integer.MAX_VALUE (File size: " + fileSize + ")");
		
		final var buffer = allocator.apply((int) fileSize).clear();
		if(buffer.capacity() != fileSize)
			throw new IllegalArgumentException("allocator function returned buffer with size " 
					+ buffer.capacity() + " when " + fileSize + " was requested");
		
		try(var channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
			channel.read(buffer);
			if(channel.position() != fileSize) //Validate that we read everything
				throw new IOException("File size after prepearing data buffer for reading");
		}
		return buffer;
	}
	
	/**
	 * Loads a sequence of bytes from a {@link URL} into a {@link ByteBuffer}.
	 * @param resourceName The name or path of the resource
	 * @param allocator A function that creates {@link ByteBuffer}s with the requested capacity
	 * @return The {@link ByteBuffer} with the loaded data. The buffers properties are determined by the allocator function.
	 * @throws IOException When the file is not found or when a channel to read the file cannot be opened.
	 */
	public static ByteBuffer loadBytes(URL resourceName, IntFunction<ByteBuffer> allocator) throws IOException {
		return loadBytes(url2nio(resourceName), allocator);
	}
	
	/**
	 * Loads a sequence of bytes from a file into a {@link ByteBuffer}.
	 * @param path The {@link Path} to the file
	 * @return The {@link ByteBuffer} with the loaded data. The buffer will have a backing array.
	 * @throws IOException When the file is not found or cannot be read.
	 */
	public ByteBuffer loadBytes(Path path) throws IOException {
		return ByteBuffer.wrap(Files.readAllBytes(path));
	}
	
	/**
	 * Loads a sequence of bytes from a {@link URL} into a {@link ByteBuffer}.
	 * @param resourceName The name or path of the resource
	 * @return The {@link ByteBuffer} with the loaded data. The buffer will have a backing array.
	 * @throws IOException When the file is not found or cannot be read.
	 */
	public ByteBuffer loadBytes(URL resourceName) throws IOException {
		return loadBytes(url2nio(resourceName));
	}
	
	/**
	 * Loads a single {@link String} from a file 
	 * @param path The {@link Path} to the file
	 * @return The string with the file contents, decoded in the {@code UTF-8} charset.
	 * @throws IOException When the file is not found or cannot be read.
	 */
	public static String loadString(Path path) throws IOException {
		return loadString(path, StandardCharsets.UTF_8);
	}
	
	/**
	 * Loads a single {@link String} from a {@link URL}
	 * @param resourceName The name or path of the resource
	 * @return The string with the file contents, decoded in the {@code UTF-8} charset.
	 * @throws IOException When the file is not found or cannot be read.
	 */
	public static String loadString(URL resourceName) throws IOException {
		return loadString(url2nio(resourceName));
	}
	
	/**
	 * Loads a single {@link String} from a file 
	 * @param path The {@link Path} to the file
	 * @param charset The {@link Charset} used to decode the bytes into a String
	 * @return The string with the file contents, decoded in the requested charset.
	 * @throws IOException When the file is not found or cannot be read.
	 */
	public static String loadString(Path path, Charset charset) throws IOException {
		return Files.readString(path, charset);
	}
	
	/**
	 * Loads a single {@link String} from a {@link URL} 
	 * @param resourceName The name or path of the resource
	 * @param charset The {@link Charset} used to decode the bytes into a String
	 * @return The string with the file contents, decoded in the requested charset.
	 * @throws IOException When the file is not found or cannot be read.
	 */
	public static String loadString(URL resourceName, Charset charset) throws IOException {
		return loadString(url2nio(resourceName), charset);
	}
}
