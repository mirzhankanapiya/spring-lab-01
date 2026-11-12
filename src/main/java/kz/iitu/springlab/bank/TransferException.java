package kz.iitu.springlab.bank;

public class TransferException extends Exception {
    public TransferException(String message) {
        super(message);
    }
}