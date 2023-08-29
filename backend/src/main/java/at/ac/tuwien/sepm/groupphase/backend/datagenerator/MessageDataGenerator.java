package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.entity.Message;
import at.ac.tuwien.sepm.groupphase.backend.repository.MessageRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Profile("generateData")
@Component
public class MessageDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String TEST_NEWS_IMAGE = "data:image/png;base64,UklGRj4LAABXRUJQVlA4WAoAAAAgAAAApwQAyAIASUNDUMgBAAAAAAHIAAAAAAQwAABtbnRyUkdCIFhZWiAH4AABAAEAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAAAAAA\n"
        + "AAAAAAAAAAAAAQAA9tYAAQAAAADTLQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAACRyWFlaAAABFAAA\n"
        + "ABRnWFlaAAABKAAAABRiWFlaAAABPAAAABR3dHB0AAABUAAAABRyVFJDAAABZAAAAChnVFJDAAABZAAAAChiVFJDAAABZAAAAChjcHJ0AAABjAAAADxtbHVjAAAAAAAA\n"
        + "AAEAAAAMZW5VUwAAAAgAAAAcAHMAUgBHAEJYWVogAAAAAAAAb6IAADj1AAADkFhZWiAAAAAAAABimQAAt4UAABjaWFlaIAAAAAAAACSgAAAPhAAAts9YWVogAAAAAAAA\n"
        + "9tYAAQAAAADTLXBhcmEAAAAAAAQAAAACZmYAAPKnAAANWQAAE9AAAApbAAAAAAAAAABtbHVjAAAAAAAAAAEAAAAMZW5VUwAAACAAAAAcAEcAbwBvAGcAbABlACAASQBu\n"
        + "AGMALgAgADIAMAAxADZWUDggUAkAABDYAJ0BKqgEyQI+0WivUygmJCKg+PgRABoJaW7hd2Eas8+U7qyAwKyhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDh8o\n"
        + "omSglfJudQmSUBR+9JNzqEySgKP3pJudQmSUBR+9JNzqEySgKQLH6lBH2Dmn1Dhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4\n"
        + "cOHDhw4cOHyiiVeaexZ06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dO\n"
        + "nTp06dOnTp06dPPCBlWDmn1Dhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOH\n"
        + "Dhw4cOHDhw4cOHDhw+UUSrzT2LOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp\n"
        + "06dOnTp06dOnTp06dOnTqtNRVg5p9Q4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw\n"
        + "4cOHDhw4cOHDhw4cOHDhw4cOPYaiVeaexZ06dOnTpviZ5hmeuIkU+1fIPRfavkHovtXyD0UEru8qdA8IGVYOafUOHDhw4cOHDhw4cOHDhw4cOHDhwyNqoQpPUgerI3pv\n"
        + "pvpvpvpvpvppXyRy6njHZM6dOnTp06dOnTp06dOnTp06dOnTp06dOnTo6t0XFFdElSTl098n1GwteA9Junz/KprzNyEs6dOnTp06dOnTp06dVpqKsHNPqHDhw4cOG/Ms\n"
        + "GKg39hud5HDzOKHW2GrQEmobAIZkJP94Jm5CWdOnTp06dOnTp06dOnTp06dOnTp06dOnTpvufm+tCgkD+XgPwUrpMgKPCyeNhDOACf8OAE9Rh+O0zchLOnTp06dOnTp0\n"
        + "6dOnTp06dOnTp06dOnTp033Pzich3GO5IAHEVR0gtQ6BSC+rhmbkJZ06dOnTp06dOnTp06rTUVYOafUOHDhw4cN+ZYMVDxUbEAxGAYGcQHP+A9fAF6D0O+6Z+i9izp06\n"
        + "dOnTp06dOnT0FBlWDmn1Dhw4cOHDhkkNJH5OtnMYtdz3NMIx+Z+i9izp06dOnTp06dOnTp06dOnTp06dOnTp06b7n4wN5dEgVeAa/d/eFiFiFiFiFhb8xZQD7jG97FnT\n"
        + "p06dOnTp06dOnTp06dOnTp06dOnTp033Sy47JKSORIeMdkzp06dOnTp06dOnTp088IGVYOafUOHDhw4cMkjhELUi2LiMdkzp06dOnTp06dOnTp1XggZVg5p9Q4cOHDhw\n"
        + "35mVuVIG8ieLjHZM6dOnTp06dOnTp06dOnTp06dOnTp06dOnTo4GXpXd8Wq+Qei+1fIPRfavkHovtSEPiJZI+dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06duZFCbx5\n"
        + "6KbzT2LOnTp06dOnTp06eeEDKsHNPqHDhw4cOHEG187Sk0lxnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp\n"
        + "06dOnTp06dOnTp06dOnTp06dOnVaairBzT6hw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw\n"
        + "4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cew1Eq809izp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06d\n"
        + "OnTp06dOnTp06dOnTp06dOnTp06dOnTp088IGVYOafUOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cO\n"
        + "HDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHD5RRKvNPYs6dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnT\n"
        + "p06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dHAAD+/7HU/6txBfIOabkX5RVb8oqt+UVW/KKrflFVvyiq35RVb8oqt+UVW/KKrflFVvyiq35RVb8oqt+UVW/KKrfl\n"
        + "FVvyiyNGDE4AAAABHgQAL2rAgAOVAAPkCAAAAAUgoABrgQAAAACkFAANcCAAAAAUgoABrgQAAByO9mPF4vF4vF4vECTYo28JdZ7z9LpIxhpb53Hw3i6J9LQMnOr5y5Od\n"
        + "Xzlyc6N1xf4i2ppKmEmBBNWDtqsOsEsteTDyH1dLaXxaXkKGkgdFfgOxl/G1dTOoPFF6ixjwATo/+itYwMSLfUN/6Hxv3y9BU0Y6XfVyRQcA8tqf4VVVaLE55pMkSzJe\n"
        + "i9lkf6vF81WiWlD1FBgReilqB1b7UQeQmv27zu16n+4W2XgfviE5G4rulHAX9ASmH9WrCQcX9vAkBAkgdsCXch4xcgHOPrT/cGz+28cJVxK50KXywNu/PEAWdrvhV7CZ\n"
        + "7mb+rfnOxmvFoPNrOpMeizVgIAJuC5IUBWB2Qmat3qfSy3xdUeGpQn/r6XruziNysotRaGXyTLPd5N0AT8fgo9BS1AgQv3bgCkrNl/HbGN2uTG/oCIboHX98vshNJ1Fb\n"
        + "ytiKi+wFNirwFsl3jzwvsBkXChsLAFHLrtatAyjQAjjy09P/nElDS1gFfVlB018N/eGlWKRIhxc623bbwnpbbwnphIooSjER4AmDZHLgqt8CAAjPAfouSLuHz09srq0X\n"
        + "gpar8s+O45gzV8BNYFxqHzLeJjKTQFOfxoL+6OopKNJerK8VRCgAHzLvF4vF4vF4vF2yIFKigADdAgAAAAFIKAAa4EAAAAApBQADXAgAAAAFIKAAa4EAAAAApBQADXAg\n"
        + "AAAAAAAA";

    private final MessageRepository messageRepository;

    public MessageDataGenerator(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void generateMessage() {
        if (!messageRepository.findAll().isEmpty()) {
            LOGGER.debug("message already generated");
        } else {
            LOGGER.debug("generating news entries");
            List<Message> messageList = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                Message message = Message.generateRandomMessage();
                message.setImage(TEST_NEWS_IMAGE);
                messageList.add(message);
            }
            messageRepository.saveAll(messageList);
        }
    }
}
