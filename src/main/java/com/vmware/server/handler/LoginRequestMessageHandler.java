package com.vmware.server.handler;

import com.vmware.message.LoginRequestMessage;
import com.vmware.message.LoginResponseMessage;
import com.vmware.server.service.UserServiceFactory;
import com.vmware.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        String username = msg.getUsername();
        String password = msg.getPassword();
        boolean login = UserServiceFactory.getUserService().login(username, password);
        if (login) {
            SessionFactory.getSession().bind(ctx.channel(), username);
        }
        ctx.channel().writeAndFlush(new LoginResponseMessage(login, login ? "登录成功!" : "登录失败,用户名或密码有误!"));
    }
}
