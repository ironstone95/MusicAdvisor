package advisor;

import java.util.Arrays;
import java.util.Scanner;

public class CLI {
    private final Scanner scanner;
    private final MusicAdvisorController controller;
    private String lastCommand;
    private int page = 0;
    private int lastValidPage = 0;

    public CLI(String[] args) {
        scanner = new Scanner(System.in);
        this.controller = new MusicAdvisorController(args);
    }

    public void start() {
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equals("next")) {
                if (onNextEvent()) {
                    processInput(lastCommand);
                }
            } else if (input.equals("prev")) {
                if (onPreviousEvent()) {
                    processInput(lastCommand);
                }
            } else {
                if (processInput(input)) {
                    break;
                }
            }
        }
    }

    private boolean processInput(String input) {
        if (lastCommand == null || !lastCommand.equals(input)) {
            page = 0;
        }
        if (input != null) {
            String[] seperated = input.split(" ");
            switch (seperated[0]) {
                case "auth":
                    lastCommand = "auth";
                    onAuthEvent();
                    break;
                case "new":
                    lastCommand = "new";
                    onNewReleasesEvent();
                    break;
                case "featured":
                    lastCommand = "featured";
                    onFeaturedEvent();
                    break;
                case "categories":
                    lastCommand = "categories";
                    onCategoriesEvent();
                    break;
                case "playlists":
                    String[] inputArr = input.split(" ");
                    if (inputArr.length > 1) {
                        lastCommand = String.join(" ", input);
                        String category = String.join(" ", Arrays.copyOfRange(inputArr, 1, inputArr.length));
                        onPlaylistsEvent(category);
                    } else {
                        System.out.println("You must enter a category name. To see available categories enter 'categories'.");
                    }
                    break;
                case "exit":
                    controller.exit();
                    System.out.println("---GOODBYE!---");
                    return true;
            }
        }
        return false;
    }

    private void onAuthEvent() {
        controller.auth();
    }

    private boolean onNextEvent() {
        if (lastCommand == null) {
            showIllegalPageChangeMessage();
            return false;
        } else {
            page++;
            return true;
        }
    }

    private boolean onPreviousEvent() {
        if (lastCommand == null) {
            showIllegalPageChangeMessage();
            return false;
        } else {
            page--;
            return true;
        }
    }

    private void showIllegalPageChangeMessage() {
        System.out.println("There is not a list to be shown.");
    }

    private void onNewReleasesEvent() {
        processResult(controller.getNewReleases(page));
    }

    private void onFeaturedEvent() {
        processResult(controller.getFeaturedLists(page));
    }

    private void onCategoriesEvent() {
        processResult(controller.getCategoryList(page));
    }

    private void onPlaylistsEvent(String categoryName) {
        processResult(controller.getPlaylists(page, categoryName));
    }

    private void processResult(int resultCode) {
        switch (resultCode) {
            case 0:
                printList(controller.getPagedList(page));
                break;
            case 1:
                authMessage();
                break;
            case 2:
                noMorePagesMessage();
                break;
            case 3:
                System.out.println("Unknown category id.");
                break;
        }
    }

    private void printList(String listText) {
        if (listText != null) {
            lastValidPage = page;
            System.out.println(listText);
        } else {
            noMorePagesMessage();
        }
    }

    private void authMessage() {
        System.out.println("Please, provide access for application.");
    }

    private void noMorePagesMessage() {
        System.out.println("No more pages...");
        page = lastValidPage;
    }
}
