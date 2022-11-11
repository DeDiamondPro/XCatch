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

package dev.dediamondpro.xcatch.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.dediamondpro.xcatch.XCatch;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PersistentData {
    private static final Gson gson = new Gson();
    public static Data data = new Data();

    public static class Data {
        public int totalFlags = 0;
        public int totalBans = 0;
        public HashMap<UUID, ArrayList<ActionData>> actions = new HashMap<>();
    }

    public static void saveData(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filePath))))) {
            writer.write(gson.toJson(data));
        } catch (Exception e) {
            XCatch.INSTANCE.logger.log(Level.SEVERE, "Failed to save XCatch data.");
        }
    }

    public static void loadData(String filePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath))))) {
            Type type = new TypeToken<Data>() {
            }.getType();
            data = gson.fromJson(reader, type);
        } catch (Exception e) {
            XCatch.INSTANCE.logger.log(Level.SEVERE, "Failed to load XCatch data.");
        }
    }
}
