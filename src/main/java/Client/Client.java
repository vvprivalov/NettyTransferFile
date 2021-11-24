package Client;

import Common.Handler.JsonDecoder;
import Common.Handler.JsonEncoder;
import Common.Messages.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Client {
    private static int i = 0;       // счетчик переданных пакетов

    public static void main(String[] args) throws InterruptedException {
        new Client().start();
    }

    public void start() {
        final NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0,
                                            3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new SimpleChannelInboundHandler<Message>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
                                            System.out.println("Новый входящий пакет файла - " + (i++));
                                            if (msg instanceof FileTransferMessage) {
                                                FileTransferMessage message = (FileTransferMessage) msg;
                                                try (RandomAccessFile rw = new RandomAccessFile("1.pdf", "rw")) {
                                                    rw.seek(message.getStartPosition());
                                                    rw.write(message.getContent());
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            if (msg instanceof EndFileTransferMessage) {
                                                System.out.println("Передача файла закончена");
                                                ctx.close();
                                            }
                                        }
                                    }
                            );
                        }
                    });

            System.out.println("Клиент запущен");

            ChannelFuture channelFuture = bootstrap.connect("localhost", 9000).sync();
            channelFuture.channel().writeAndFlush(new RequestFileMessage());
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
