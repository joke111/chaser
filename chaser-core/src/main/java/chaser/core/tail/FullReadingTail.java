package chaser.core.tail;

import chaser.core.file.ChaseFile;
import chaser.util.ByteUtils;
import chaser.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FullReadingTail implements Tail {

	public Byte[] read(ChaseFile target) {
		RandomAccessFile randomAccessFile = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int streamPosition = 0;
		try {
			File targetFile = target.getPath().toFile();
			byte[] buffer = newBuffer();

			do {
				long position = target.getPosition();
				if (targetFile.length() < target.getPosition()) {
					position = 0;
				}

				randomAccessFile = new RandomAccessFile(targetFile, "r");
				randomAccessFile.seek(position);
				int readCount;
				while ((readCount = randomAccessFile.read(buffer)) != -1) {
					byteArrayOutputStream.write(buffer, streamPosition, readCount);
					streamPosition += readCount;
					buffer = newBuffer();
				}
				target.setPosition(randomAccessFile.getFilePointer());
			} while(targetFile.length() != target.getPosition());

			return ByteUtils.toByteArray(byteArrayOutputStream.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			IOUtils.closeQuietly(byteArrayOutputStream);
			IOUtils.closeQuietly(randomAccessFile);
		}
	}

	private byte[] newBuffer() {
		return new byte[4096];
	}

}
