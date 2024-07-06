package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";


    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final AccountService accountService = new AccountService();
    private final UserService userService = new UserService();
    private final TransferService transferService = new TransferService();

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
		// TODO Auto-generated method stub
        AccountService accountService = new AccountService();
        // Prints a formatted version of the users current balance (see console services)
        consoleService.printBalance(accountService.getAccountByUserId(currentUser, currentUser.getUser().getId()).getBalance());
	}

	private void viewTransferHistory() {
        transferService.setAuthToken(currentUser.getToken());

        Scanner scanner = new Scanner(System.in);
        System.out.println("-------------------------------------------");
        System.out.println("Transfers");
        System.out.printf("%-10s %-30s %-10s", "ID", "From/To", "Amount");
        System.out.println();
        // Getting user Account info
        Account userAccount = accountService.getAccountByUserId(currentUser, currentUser.getUser().getId());
        Transfer[] transfers = transferService.getTransfersByAccountId(userAccount.getAccountId());
        // Check if transfers array is null
        if (transfers != null && transfers.length > 0) {
            for (Transfer transfer : transfers) {
                // Will also Have to get username based off the to/from
                // if it does not equal the current user
                String fromTo = "From: " + userService.getUserByAccountId(currentUser,transfer.getAccountFromId()) + " To: " + userService.getUserByAccountId(currentUser,transfer.getAccountToId());
                System.out.printf("%-10s %-30s %-10s%n", transfer.getId(), fromTo, transfer.getAmount());
                // Need to place logic to get username of sender and receiver

            }
        } else {
            System.out.println();
            System.out.println("No transfers found.");
        }
        System.out.println("---------");
        System.out.println("Please enter transfer ID to view details (0 to cancel): ");
        int transferId = scanner.nextInt();
        if (transferId == 0) {
            System.out.println("Canceling...");
        } else {
            // Getting Transfer info by transfer Id
            //Transfer transfer = transferService.getTransferById(currentUser, transferId);
            for (Transfer transfer:transfers) {
                if (transfer.getId() == transferId) {
                    System.out.println("-------------------------------------------- \n Transfer Details \n--------------------------------------------");
                    System.out.println("Id: " + transfer.getId());
                    System.out.println("From: " + userService.getUserByAccountId(currentUser, transfer.getAccountFromId()));
                    System.out.println("To: " + userService.getUserByAccountId(currentUser, transfer.getAccountToId()));
                    if (transfer.getTransferTypeId() == 1) {
                        System.out.println("Type: Request");
                    } else {
                        System.out.println("Type: Send");
                    }
                    if (transfer.getTransferStatusId() == 1) {
                        System.out.println("Status: Pending");
                    } else if (transfer.getTransferStatusId() == 2) {
                        System.out.println("Status: Approved");
                    } else {
                        System.out.println("Status: Rejected");
                    }
                    System.out.println("Amount: $" + transfer.getAmount());

                }
            }
        }

    }


	private void viewPendingRequests() {
        transferService.setAuthToken(currentUser.getToken());
        System.out.println("-------------------------------------------\n" +
                "Pending Transfers\n" +
                "ID          To                     Amount\n" +
                "-------------------------------------------");
        System.out.println();

        Account userAccount = accountService.getAccountByUserId(currentUser, currentUser.getUser().getId());
        Transfer[] transfers = transferService.getTransfersByAccountId(userAccount.getAccountId());
        for (Transfer transfer: transfers) {
            if (transfer.getAccountToId() != currentUser.getUser().getId() && transfer.getTransferStatusId() == 1) {
                System.out.printf("%-10s %-25s %-10s%n$", transfer.getId(), userService.getUserByAccountId(currentUser, transfer.getAccountToId()), transfer.getAmount());
            }
        }
        // Create the logic to complete a transfer if it's pending
        System.out.println("Please enter transfer ID to approve/reject (0 to cancel):");
        Scanner scanner = new Scanner(System.in);
        int transferId = scanner.nextInt();
        if (transferId == 0){
            System.out.println("Canceled...");
            return;
        } else {
            // Update transfer
            System.out.println("1: Approve\n" +
                    "2: Reject\n" +
                    "0: Don't approve or reject\n" +
                    "---------\n" +
                    "Please choose an option: ");
            int userInput = scanner.nextInt();
            if (userInput == 1) {
                // Update status to Success and complete transfer
                return;
            } else if (userInput == 2) {
                // Update status to Rejected
                return;
            } else {
                // Don't do either
                System.out.println("Canceling...");
                return;
            }
        }
	}

	private void sendBucks() {
		// TODO Auto-generated method stub
        consoleService.printSendRequestBanner();
        consoleService.printUserIdsAndNames(currentUser, userService.getUsers(currentUser));
        int receivingUserId = consoleService.promptForInt("Enter ID of user you are sending money to (0 to cancel): ");

        if (receivingUserId == currentUser.getUser().getId()) {
            System.out.println("You cannot send money to yourself.");
        }

        if (receivingUserId != 0 && receivingUserId != currentUser.getUser().getId()) {
            BigDecimal amountToSend = consoleService.promptForBigDecimal("Enter amount: $");
            if (accountService.getAccountByUserId(currentUser, currentUser.getUser().getId()).getBalance().subtract(amountToSend).compareTo(BigDecimal.ZERO) >= 0) {
                int currentUserAccountId = accountService.getAccountByUserId(currentUser, currentUser.getUser().getId()).getAccountId();
                int receivingUserAccountId = accountService.getAccountByUserId(currentUser, receivingUserId).getAccountId();
                Transfer transfer = new Transfer(2, 2, currentUserAccountId, receivingUserAccountId, amountToSend);
                Transfer sentTransfer = transferService.sendTransfer(currentUser, transfer);

                if (accountService.updateAccount(currentUser, sentTransfer, currentUserAccountId)) {
                    System.out.println("Transfer complete.");
                } else {
                    System.out.println("Transfer failed, make sure the user ID matches on the list and try again.");
                }

            } else {
                consoleService.printNotEnoughBalanceForTransfer();
            }
        }
	}

	private void requestBucks() {
		// TODO Auto-generated method stub
        consoleService.printSendRequestBanner();
        consoleService.printUserIdsAndNames(currentUser, userService.getUsers(currentUser));
        int requestingUserId = consoleService.promptForInt("Enter ID of user you are requesting money from (0 to cancel): ");

        if (requestingUserId == currentUser.getUser().getId()) {
            System.out.println("You cannot request money from yourself.");
        }

        if (requestingUserId != 0 && requestingUserId != currentUser.getUser().getId()) {
            BigDecimal amountToRequest = consoleService.promptForBigDecimal("Enter amount: $");
            Transfer transfer = new Transfer(1, 1,
                    accountService.getAccountByUserId(currentUser, requestingUserId).getAccountId(),
                    accountService.getAccountByUserId(currentUser, currentUser.getUser().getId()).getAccountId(),
                    amountToRequest);
            Transfer requestedTransfer = transferService.sendTransfer(currentUser, transfer);
            try {
                if (transferService.getTransferStatusDescriptionById(currentUser,
                        requestedTransfer.getTransferStatusId()).equals("Pending")) {
                    System.out.println("Request was sent and is pending for approval from user.");
                } else {
                    System.out.println("Request failed.");
                }
            } catch (NullPointerException e) {
                System.out.println("Could not find user with specified ID.");
            }
        }
		
	}

}
