/*
 * This file is part of XCatch.
 *
 * XCatch is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * XCatch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see
 * <https://www.gnu.org/licenses/>.
 */

package dev.dediamondpro.xcatch.utils;

import dev.dediamondpro.xcatch.XCatch;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebhookHandler {

    public static void sendWebhook(String content) {
        try {
            String username = XCatch.config.getString("webhook-username");
            String avatar = XCatch.config.getString("webhook-avatar");
            byte[] bytes = ("{\"username\":\"" + username + "\",\"avatar_url\":\"" + avatar + "\",\"content\":\"" + content + "\"}").getBytes(StandardCharsets.UTF_8);
            URL url = new URL(XCatch.config.getString("webhook-url"));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setFixedLengthStreamingMode(bytes.length);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.connect();
            try (OutputStream out = con.getOutputStream()) {
                out.write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
