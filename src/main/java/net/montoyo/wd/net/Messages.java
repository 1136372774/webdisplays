/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class Messages {

    private static DefaultHandler DEFAULT_HANDLER = new DefaultHandler();
    private static Class<? extends IMessage>[] messages;
    static {
        ArrayList<Class<? extends IMessage>> l = new ArrayList<>();
        l.add(CMessageAddScreen.class);
        l.add(SMessageRequestTEData.class);
        l.add(SMessageScreenCtrl.class);
        l.add(CMessageOpenGui.class);
        l.add(CMessageScreenUpdate.class);
        l.add(SMessageACQuery.class);
        l.add(CMessageACResult.class);
        l.add(SMessagePadCtrl.class);

        messages = l.toArray(new Class[0]);
    }

    public static void registerAll(SimpleNetworkWrapper wrapper) {
        for(Class<? extends IMessage> md: messages) {
            Message data = md.getAnnotation(Message.class);
            if(data == null)
                throw new RuntimeException("Missing @Message annotation for message class " + md.getSimpleName());

            Class<?>[] classes = md.getClasses();
            Class<? extends IMessageHandler> handler = null;

            for(Class<?> cls: classes) {
                if(cls.getSimpleName().equals("Handler") && Modifier.isStatic(cls.getModifiers()) && IMessageHandler.class.isAssignableFrom(cls)) {
                    handler = (Class<? extends IMessageHandler>) cls;
                    break;
                }
            }

            IMessageHandler handlerInst;
            if(handler == null) {
                if(Runnable.class.isAssignableFrom(md))
                    handlerInst = DEFAULT_HANDLER;
                else
                    throw new RuntimeException("Could not find message handler for message " + md.getSimpleName());
            } else {
                try {
                    handlerInst = handler.newInstance();
                } catch(Throwable t) {
                    throw new RuntimeException("Could not instantiate message handler for message " + md.getSimpleName());
                }
            }

            wrapper.registerMessage(handlerInst, md, data.messageId(), data.side());
        }
    }

}