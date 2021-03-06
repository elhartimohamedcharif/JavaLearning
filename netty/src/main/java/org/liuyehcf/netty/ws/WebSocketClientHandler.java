package org.liuyehcf.netty.ws;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

/**
 * @author hechenfeng
 * @date 2018/11/3
 */
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    /**
     * netty build-in web socket hand shaker
     */
    private final WebSocketClientHandshaker handShaker;

    /**
     * future on where hand shaker is completed
     */
    private ChannelPromise handshakeFuture;

    public WebSocketClientHandler(WebSocketClientHandshaker handShaker) {
        this.handShaker = handShaker;
    }

    public ChannelFuture handshakeFuture() {
        return this.handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // execution timing must after all the handlers are added
        // other wise exception may occurred (ChannelPipeline does not contain a HttpRequestEncoder or HttpClientCodec)
        handShaker.handshake(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        final Channel channel = ctx.channel();
        final FullHttpResponse response;
        if (!this.handShaker.isHandshakeComplete()) {
            try {
                response = (FullHttpResponse) msg;
                this.handShaker.finishHandshake(channel, response);

                // listeners is going to be trigger
                this.handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException var7) {
                FullHttpResponse res = (FullHttpResponse) msg;
                String errorMsg = String.format("webSocket Client failed to connect. status='%s'; reason='%s'", res.status(), res.content().toString(CharsetUtil.UTF_8));

                // listeners is going to be trigger
                this.handshakeFuture.setFailure(new Exception(errorMsg));
            }
        } else if (msg instanceof FullHttpResponse) {
            response = (FullHttpResponse) msg;
            throw new IllegalStateException("unexpected FullHttpResponse (getStatus=" + response.status() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        } else if (msg instanceof TextWebSocketFrame) {
            ctx.fireChannelRead(((TextWebSocketFrame) msg).retain());
        } else if (msg instanceof BinaryWebSocketFrame) {
            ctx.fireChannelRead(((BinaryWebSocketFrame) msg).retain());
        } else if (msg instanceof ContinuationWebSocketFrame) {
            // do nothing
        } else if (msg instanceof PingWebSocketFrame) {
            // do nothing
        } else if (msg instanceof PongWebSocketFrame) {
            // do nothing
        } else if (msg instanceof CloseWebSocketFrame) {
            channel.close();
        } else {
            throw new IllegalStateException("unexpected MessageType='" + msg.getClass() + "'");
        }
    }
}