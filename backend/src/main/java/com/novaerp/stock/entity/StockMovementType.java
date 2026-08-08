package com.novaerp.stock.entity;

public enum StockMovementType {
    IN_PURCHASE,      // ENTREE
    OUT_SALE,         // SORTIE
    ADJUSTMENT_IN,    // AJUSTEMENT +
    ADJUSTMENT_OUT,   // AJUSTEMENT -
    TRANSFER,         // TRANSFERT
    RETURN            // RETOUR
}
