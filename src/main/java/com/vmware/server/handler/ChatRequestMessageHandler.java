package com.vmware.server.handler;

import com.vmware.message.ChatRequestMessage;
import com.vmware.message.ChatResponseMessage;
import com.vmware.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ChatRequestMessage chatRequestMessage) throws Exception {
        String to = chatRequestMessage.getTo();
        String from = chatRequestMessage.getFrom();
        String content = chatRequestMessage.getContent();
        Channel channel = SessionFactory.getSession().getChannel(to);
        if (channel!=null){
            //转发消息
            channel.writeAndFlush(new ChatResponseMessage(from,content));
        }
        else {
            channelHandlerContext.writeAndFlush(new ChatResponseMessage(false,"发送失败,用户不在线!"));
        }
    }
}
