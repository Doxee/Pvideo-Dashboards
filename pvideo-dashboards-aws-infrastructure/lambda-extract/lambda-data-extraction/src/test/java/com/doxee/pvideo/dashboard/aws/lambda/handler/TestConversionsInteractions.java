package com.doxee.pvideo.dashboard.aws.lambda.handler;

import org.junit.Assert;
import org.junit.Test;

import com.doxee.pvideo.dashboard.aws.lambda.handler.utils.Utils;

public class TestConversionsInteractions {

    @Test
    public void testConversionName() {

        String conversion = "CONV_02 conversione scena 3";

        String name = Utils.getConversionOrInteractionName(conversion);

        Assert.assertEquals(name, "conversione scena 3");

    }

    @Test
    public void testInteractionName() {

        String interaction = "INTE_02 Contains the alphabet to test fonts";

        String name = Utils.getConversionOrInteractionName(interaction);

        Assert.assertEquals(name, "Contains the alphabet to test fonts");

        interaction = "INTER_03 Testo mio testo 1234 CONV";

        name = Utils.getConversionOrInteractionName(interaction);

        Assert.assertEquals(name, "Testo mio testo 1234 CONV");

    }
}
