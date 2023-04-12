package com.vmware.server.handler;

import com.vmware.message.GroupCreateRequestMessage;
import com.vmware.message.GroupCreateResponseMessage;
import com.vmware.message.GroupJoinResponseMessage;
import com.vmware.server.session.Group;
import com.vmware.server.session.GroupSession;
import com.vmware.server.session.GroupSessionFactory;
import com.vmware.server.session.GroupSessionMemoryImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@ChannelHandler.Sharable
@Slf4j
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, GroupCreateRequestMessage groupCreateRequestMessage) throws Exception {
        Set<String> members = groupCreateRequestMessage.getMembers();
        String groupName = groupCreateRequestMessage.getGroupName();
        GroupSession session = GroupSessionFactory.getGroupSession();
        Group group = session.createGroup(groupName, members);
        GroupCreateResponseMessage message = null;
        if (group != null) {
            message = new GroupCreateResponseMessage(false, "创建群聊失败!");
        } else {
            message = new GroupCreateResponseMessage(true, "创建群聊成功!");
            List<Channel> channels = session.getMembersChannel(groupName);
            channels.forEach(channel -> {
                GroupJoinResponseMessage joinResponseMessage = new GroupJoinResponseMessage(true, "您已被拉入群聊:" + groupName);
                channel.writeAndFlush(joinResponseMessage);
            });
        }
        channelHandlerContext.writeAndFlush(message);
    }
}
