package Server;

import Common.Messages.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FirstServerHandler extends SimpleChannelInboundHandler<Message> {
    private static final String FILE_NAME = "file";
    private static final int BUFFER_SIZE = 1024 * 64;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        System.out.println("New active channel");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof TextMessage) {
            TextMessage message = (TextMessage) msg;
            System.out.println("incoming text message: " + message.getText());
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof DateMessage) {
            DateMessage message = (DateMessage) msg;
            System.out.println("incoming date message: " + message.getDate());
            ctx.writeAndFlush(msg);
        }

        if (msg instanceof RequestFileMessage) {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(FILE_NAME, "r")) {
                final long fileLenght = randomAccessFile.length();
                do {
                    var position = randomAccessFile.getFilePointer();
                    long avialableBytes = fileLenght - position;
                    byte[] bytes;
                    if (avialableBytes >= BUFFER_SIZE) {
                        bytes = new byte[BUFFER_SIZE];
                    } else {
                        bytes = new byte[ (int) avialableBytes];
                    }
                    randomAccessFile.read(bytes);

                    FileTransferMessage message = new FileTransferMessage();
                    message.setContent(bytes);
                    message.setStartPosition(position);
                    ctx.writeAndFlush(message);


                } while (randomAccessFile.getFilePointer() < fileLenght);

                ctx.writeAndFlush(new EndFileTransferMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        System.out.println("client disconnect");
    }

}
