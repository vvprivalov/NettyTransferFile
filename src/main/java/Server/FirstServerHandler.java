package Server;

import Common.Messages.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Executor;

public class FirstServerHandler extends SimpleChannelInboundHandler<Message> {
    private static final String FILE_NAME = "file";
    private static final int BUFFER_SIZE = 1024 * 64;
    private final Executor executor;

    public FirstServerHandler(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        System.out.println("Новый пользователь подключен");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof RequestFileMessage) {
            executor.execute(() -> {
                try (RandomAccessFile randomAccessFile = new RandomAccessFile(FILE_NAME, "r")) {
                    final long fileLenght = randomAccessFile.length();
                    do {
                        var position = randomAccessFile.getFilePointer();
                        long avialableBytes = fileLenght - position;
                        byte[] bytes;
                        if (avialableBytes >= BUFFER_SIZE) {
                            bytes = new byte[BUFFER_SIZE];
                        } else {
                            bytes = new byte[(int) avialableBytes];
                        }
                        randomAccessFile.read(bytes);

                        FileTransferMessage message = new FileTransferMessage();
                        message.setContent(bytes);
                        message.setStartPosition(position);
                        ctx.writeAndFlush(message).sync();

                    } while (randomAccessFile.getFilePointer() < fileLenght);

                    ctx.writeAndFlush(new EndFileTransferMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
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
