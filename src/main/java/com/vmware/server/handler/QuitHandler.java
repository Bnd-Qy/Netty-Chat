package com.vmware.server.handler;

import com.vmware.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class QuitHandler extends ChannelInboundHandlerAdapter {
    //处理channel正常关闭事件
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //移除channel
        SessionFactory.getSession().unbind(ctx.channel());
        log.info("channel:{}已断开!",ctx.channel());
    }

    //处理channel异常关闭事件
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SessionFactory.getSession().unbind(ctx.channel());
        log.info("channel:{}异常断开!",ctx.channel());
    }
}
