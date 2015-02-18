package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.message.model.IMessageHeader;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.common.ExceptionHelper;
import com.freedom.messagebus.server.Constants;
import com.freedom.messagebus.server.daemon.DaemonService;
import com.freedom.messagebus.server.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Map;

@DaemonService(value = "msgLogService", policy = RunPolicy.ONCE)
public class MsgLogService extends AbstractService {

    private static final Log logger = LogFactory.getLog(MsgLogService.class);
    private Messagebus client;
    private AsyncConsumer asyncConsumer;

    public MsgLogService(Map<String, Object> context) {
        super(context);

        client = (Messagebus) this.context.get(Constants.GLOBAL_CLIENT_OBJECT);
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                asyncConsumer = client.getAsyncConsumer(
                    Constants.LOG_OF_FILE_QUEUE_NAME,
                    new IMessageReceiveListener() {
                        @Override
                        public void onMessage(Message message, IReceiverCloser consumerCloser) {
                            logger.info(formatLog(message.getMessageHeader()));

                        }
                    });

                asyncConsumer.startup();
                this.wait();
            }
        } catch (InterruptedException e) {
            asyncConsumer.shutdown();
        }
    }

    private String formatLog(IMessageHeader msgHeader) {
        StringBuilder sb = new StringBuilder();
        sb.append(" [id] ");
        sb.append(msgHeader.getMessageId());
        sb.append(" [type] ");
        sb.append(msgHeader.getType());
        sb.append(" [appId] ");
        sb.append(msgHeader.getAppId());
        sb.append(" [replyTo] ");
        sb.append(msgHeader.getReplyTo());

        return sb.toString();
    }
}
