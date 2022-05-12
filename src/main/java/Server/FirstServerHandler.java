package Server;

import Common.Messages.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FirstServerHandler extends SimpleChannelInboundHandler<Message> {
    private static final int BUFFER_SIZE = 64 * 1024;
    RandomAccessFile accessFile;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        System.out.println("Новый пользователь подключен");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        System.out.println("Клиент отключился");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws IOException {
        if (msg instanceof RequestFileMessage) {
            RequestFileMessage rfm = (RequestFileMessage) msg;
            File file = new File(rfm.getPath());
            accessFile = new RandomAccessFile(file, "r");
            sendFile(ctx);
            accessFile.close();
            accessFile = null;
        }
    }

    private void sendFile(ChannelHandlerContext ctx) throws IOException {
        if (accessFile != null) {
            byte[] fileContent;
            long availableBytes = accessFile.length() - accessFile.getFilePointer();
            if (availableBytes >= BUFFER_SIZE) {
                fileContent = new byte[BUFFER_SIZE];
            } else {
                fileContent = new byte[(int) availableBytes];
            }
            FileTransferMessage message = new FileTransferMessage();
            message.setStartPosition(accessFile.getFilePointer());
            accessFile.read(fileContent);
            message.setContent(fileContent);
            final boolean last = accessFile.getFilePointer() == accessFile.length();
            message.setLast(last);
            ctx.channel().writeAndFlush(message).addListener((ChannelFutureListener) channelFuture -> {
                if (!last) {
                    sendFile(ctx);
                }
            });
        }
    }
}
