package com.raymondpang365.utility.database.creditCheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public final class CreditCheck implements ICreditCheck {
    private static final Logger logger = LoggerFactory.getLogger(CreditCheck.class);

    private final Connection connection;

    public CreditCheck(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean hasEnoughCredit(final double credit) {
        try (final CallableStatement stm = connection.prepareCall("{? = call hasEnoughCredit(?,?)}")) {
            stm.registerOutParameter(1, Types.BOOLEAN);
            stm.setString(2, "TRADING_COUNTERPARTY");
            stm.setDouble(3, credit);
            stm.execute();

            final short result = stm.getShort(1);
            logger.info("Has credit: {}", result);
            return result > 0;
        } catch (final Exception ex) {
            logger.warn(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void setCredit(final double credit) {
        try (final CallableStatement stm = connection.prepareCall("{call setCredit(?,?)}")) {
            stm.setString(1, "TRADING_COUNTERPARTY");
            stm.setDouble(2, credit);
            stm.execute();
        } catch (final Exception ex) {
            logger.warn(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void closeConnection() throws SQLException {
        connection.close();
    }
}
