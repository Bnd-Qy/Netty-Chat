package com.vmware.client;


import com.vmware.message.*;
import com.vmware.protocol.MessageCodecSharable;
import com.vmware.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);
        AtomicBoolean LOGIN = new AtomicBoolean(false);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            new Thread(() -> {
                                Scanner scanner = new Scanner(System.in);
                                System.out.print("username:");
                                String username = scanner.nextLine();
                                System.out.print("password:");
                                String password = scanner.nextLine();
                                LoginRequestMessage request = new LoginRequestMessage(username, password);
                                ctx.channel().writeAndFlush(request);
                                //阻塞
                                try {
                                    WAIT_FOR_LOGIN.await();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                if (LOGIN.get()) {
                                    while (true) {
                                        System.out.println("==================================");
                                        System.out.println("send [username] [content]");
                                        System.out.println("gsend [group name] [content]");
                                        System.out.println("gcreate [group name] [m1,m2,m3...]");
                                        System.out.println("gmembers [group name]");
                                        System.out.println("gjoin [group name]");
                                        System.out.println("gquit [group name]");
                                        System.out.println("quit");
                                        System.out.println("==================================");
                                        String command = scanner.nextLine();
                                        if (command != null && command.length() > 0) {
                                            String[] commandList = command.split(" ");
                                            switch (commandList[0]) {
                                                case "send":
                                                    ChatRequestMessage message = new ChatRequestMessage(username, commandList[1], commandList[2]);
                                                    ctx.channel().writeAndFlush(message);
                                                    break;
                                                case "gsend":
                                                    GroupChatRequestMessage groupMessage = new GroupChatRequestMessage(username, commandList[1], commandList[2]);
                                                    ctx.channel().writeAndFlush(groupMessage);
                                                    break;
                                                case "gcreate":
                                                    String members = commandList[2];
                                                    if (members != null && members.length() > 0) {
                                                        String[] memberList = members.split(",");
                                                        Set<String> memberSet = Arrays.stream(memberList).collect(Collectors.toSet());
                                                        GroupCreateRequestMessage groupCreateRequestMessage = new GroupCreateRequestMessage(commandList[1], memberSet);
                                                        ctx.channel().writeAndFlush(groupCreateRequestMessage);
                                                    }
                                                    break;
                                                case "gmembers":
                                                    GroupMembersRequestMessage groupMembersRequestMessage = new GroupMembersRequestMessage(commandList[1]);
                                                    ctx.channel().writeAndFlush(groupMembersRequestMessage);
                                                    break;
                                                case "gjoin":
                                                    GroupJoinRequestMessage groupJoinRequestMessage = new GroupJoinRequestMessage(username, commandList[1]);
                                                    ctx.channel().writeAndFlush(groupJoinRequestMessage);
                                                    break;
                                                case "gquit":
                                                    GroupQuitRequestMessage groupQuitRequestMessage = new GroupQuitRequestMessage(username, commandList[1]);
                                                    ctx.channel().writeAndFlush(groupQuitRequestMessage);
                                                    break;
                                                case "quit":
                                                    ctx.channel().close();
                                                    return;
                                            }
                                        }
                                    }
                                } else {
                                    ctx.channel().close();
                                }
                            }).start();
                        }

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.info("msg:{}", msg);
                            if (msg instanceof LoginResponseMessage response) {
                                LOGIN.set(response.isSuccess());
                                WAIT_FOR_LOGIN.countDown();
                            }
                        }
                    });
                }
            });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
