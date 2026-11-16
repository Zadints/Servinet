package org.example.servinet.infrastructure.database.sqlite;

import net.bytebuddy.implementation.bytecode.Throw;
import org.example.servinet.application.dto.database.DatabaseSetup;
import org.example.servinet.application.enums.DeploymentMode;
import org.example.servinet.application.port.out.sql.SqlCacheManagerRepository;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;
import org.example.servinet.infrastructure.dto.DatabaseConfig;
import org.sqlite.SQLiteErrorCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SqlCacheManager implements SqlCacheManagerRepository {
    private final DatabaseFactory databaseFactory;

    public SqlCacheManager(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }
    @Override
    public Optional<DeploymentMode> getDeployModeType(){
        String sql = "SELECT mode FROM database_config";

        try(Connection conn = databaseFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){

            if (!rs.next()){
                return Optional.empty();
            }

            return Optional.ofNullable(rs.getString("mode")).map(
                    value -> DeploymentMode.valueOf(value));

        }catch (SQLException e){
            if (e instanceof org.sqlite.SQLiteException sqliteEx) {

                if (sqliteEx.getResultCode()
                        == org.sqlite.SQLiteErrorCode.SQLITE_ERROR
                        && sqliteEx.getMessage().contains("no such table:")) {

                    System.out.println("La tabla no existe");
                    return Optional.empty();
                }
            }
                throw new DatabaseException("Error en SQLite", e);

        }
    }

    @Override
    public Optional<DatabaseConfig> getDatabaseConfig(){
        String sql = "SELECT host,port, database_name, username, password FROM database_config";

        try(Connection coon = databaseFactory.getConnection();
            PreparedStatement ps = coon.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();) {

            if (rs.next()){
                return Optional.empty();
            }



            return Optional.of(new DatabaseConfig(
                    rs.getString("host"),
                    rs.getInt("port"),
                    rs.getString("database"),
                    rs.getString("username"),
                    rs.getString("password")

            ));

        }catch (SQLException e){
            System.out.println(e.getMessage()+ e.getCause());
            throw new DatabaseException("No se pudo obtener als  credenciales de sql server del cache");
        }
    }
    @Override
    public void updateDatabaseConfig(DatabaseSetup db){
        String sql = """
                INSERT INTO database_config (
                    mode,
                    host,
                    port,
                    database_name,
                    username,
                    password
                )
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    mode = excluded.mode,
                    host = excluded.host,
                    port = excluded.port,
                    database_name = excluded.database_name,
                    username = excluded.username,
                    password = excluded.password,
                    updated_at = CURRENT_TIMESTAMP;
                
                """;

        try (Connection coon = databaseFactory.getConnection();
                PreparedStatement ps = coon.prepareStatement(sql)){
            ps.setString(0, db.mode().toString());
            ps.setString(1, db.host());
            ps.setInt(2, db.port());
            ps.setString(3, db.database());
            ps.setString(4, db.username());
            ps.setString(5, db.password());

            ps.executeUpdate();

        }catch (SQLException e){
            System.out.println(e.getMessage()+ e.getCause());
            throw new DatabaseException("No se pudo obtener las  credenciales de sql server del cache");
        }
    }
}


