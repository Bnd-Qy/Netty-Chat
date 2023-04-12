package com.vmware.server.handler;

import com.vmware.message.GroupChatRequestMessage;
import com.vmware.message.GroupChatResponseMessage;
import com.vmware.server.session.GroupSession;
import com.vmware.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupChatMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, GroupChatRequestMessage groupChatRequestMessage) throws Exception {
        String groupName = groupChatRequestMessage.getGroupName();
        String msg = groupChatRequestMessage.getContent();
        String from = groupChatRequestMessage.getFrom();
        List<Channel> channels = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
        channels.forEach(channel -> {
            channel.writeAndFlush(new GroupChatResponseMessage(from, msg));
        });
    }
}
