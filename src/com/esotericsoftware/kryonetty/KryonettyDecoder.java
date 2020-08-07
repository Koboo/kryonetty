package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.util.List;

public class KryonettyDecoder extends ByteToMessageDecoder {

	private final Endpoint endpoint;

	public KryonettyDecoder (Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	protected void decode (ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out)
			throws Exception {

		if (buffer.readableBytes() < 2)
			return;

		buffer.markReaderIndex();

		int len = buffer.readUnsignedShort();

		if (buffer.readableBytes() < len) {
			buffer.resetReaderIndex();
			return;
		}

		byte[] buf = new byte[len];
		buffer.readBytes(buf);

		Kryo kryo = endpoint.getKryoHolder().getKryo();
		Input input = endpoint.getKryoHolder().getInput();
		input.setInputStream(new ByteArrayInputStream(buf));
		Object object = kryo.readClassAndObject(input);
		input.close();
		endpoint.getKryoHolder().freeKryo(kryo);
		endpoint.getKryoHolder().freeInput(input);
		out.add(object);
	}
}
