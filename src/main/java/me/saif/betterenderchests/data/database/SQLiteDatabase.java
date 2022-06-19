package me.saif.betterenderchests.data.database;

import java.io.File;
import java.io.IOException;

public class SQLiteDatabase extends SQLDatabase {

    private File folder;
    private String fileName;

    public SQLiteDatabase(File folder, String fileName) {
        this.folder = folder;
        this.fileName = fileName;

        if (!this.folder.isDirectory())
            this.folder.mkdirs();

        File file = new File(this.folder, this.fileName);
        try {
            if (!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataSource.setJdbcUrl("jdbc:sqlite:" + folder.toPath().toAbsolutePath().resolve(fileName));
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.setPoolName("[VariableEnderChests-SQLite]");
    }

    @Override
    public String getType() {
        return "SQLite";
    }
}
