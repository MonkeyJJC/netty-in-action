package nia.chapter10;

import io.netty.channel.CombinedChannelDuplexHandler;

/**
 * Listing 10.10 CombinedChannelDuplexHandler<I,O>
 * 编解码器
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */

public class CombinedByteCharCodec extends CombinedChannelDuplexHandler<ByteToCharDecoder, CharToByteEncoder> {
    public CombinedByteCharCodec() {
        super(new ByteToCharDecoder(), new CharToByteEncoder());
    }
}
