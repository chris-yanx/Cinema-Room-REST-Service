package com.example.cinema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.*;

@SpringBootApplication
public class CinemaApplication {
    public static void main(String[] args) {
        SpringApplication.run(CinemaApplication.class, args);
    }

}

@RestController
class RoomController {
    private Room room = new Room();
    private HashMap<UUID, Seat> purchasedTickets = new HashMap<>();

    private Stats stats = new Stats();

    @GetMapping("/seats")
    public Room getRoomAsJson() {
        return room;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestParam(required = false) String password) {
        if (password == null || !password.equals("super_secret")) {
            return new ResponseEntity<>(Map.of("error", "The password is wrong!"), HttpStatus.UNAUTHORIZED);
        } else {
            return ResponseEntity.ok(stats);
        }
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> postSeat(@RequestBody Seat seat) {
        UUID token = UUID.randomUUID();
        int row = seat.getRow();
        int col = seat.getCol();
        Seat purchasedSeat = room.getSeat(row, col);

        if (row <= 0 || row > 9 || col <= 0 || col > 9) {
            return new ResponseEntity<>(Map.of("error", "The number of a row or a column is out of bounds!"), HttpStatus.BAD_REQUEST);
        }

        if (purchasedSeat == null) {
            return new ResponseEntity<>(Map.of("error", "The ticket has been already purchased!"), HttpStatus.BAD_REQUEST);
        }

        Ticket ticket = new Ticket(token, purchasedSeat);
        purchasedTickets.put(token, purchasedSeat);
        stats.ticketIncrement(purchasedSeat);
        room.removeSeat(row, col);
        return ResponseEntity.ok(ticket);
    }


    @PostMapping("/return")
    public ResponseEntity<?> postSeat(@RequestBody Map<String, UUID> inputToken) {
        UUID token = inputToken.get("token");
        Seat returnedSeat = purchasedTickets.get(token);

        if (returnedSeat == null) {
            return new ResponseEntity<>(Map.of("error", "Wrong token!"), HttpStatus.BAD_REQUEST);
        }
        purchasedTickets.remove(token);
        room.addSeat(returnedSeat);
        stats.ticketDecrement(returnedSeat);
        return ResponseEntity.ok(Map.of("returned_ticket", returnedSeat));
    }
}

class Stats{
    @JsonProperty("current_income")
    private int currentIncome;
    @JsonProperty("number_of_available_seats")
    private int numberOfAvailableSeats;
    @JsonProperty("number_of_purchased_tickets")
    private int numberOfPurchasedTickets;

    public Stats() {
        this.currentIncome = 0;
        this.numberOfAvailableSeats = 81;
        this.numberOfPurchasedTickets = 0;
    }

    public void ticketIncrement(Seat seat){
        currentIncome += seat.getPrice();
        numberOfAvailableSeats = numberOfAvailableSeats-1;
        numberOfPurchasedTickets += 1;
    }

    public void ticketDecrement(Seat seat){
        currentIncome -= seat.getPrice();
        numberOfAvailableSeats = numberOfAvailableSeats+1;
        numberOfPurchasedTickets -= 1;
    }
}

class Ticket {
    @JsonProperty("token")
    private UUID token;
    @JsonProperty("ticket")
    private Seat seat;

    public Ticket(UUID token, Seat seat) {
        this.token = token;
        this.seat = seat;
    }
}

class Seat {
    @JsonProperty("row")
    private int row;
    @JsonProperty("column")
    private int col;
    @JsonProperty("price")
    private int price;

    public Seat(int row, int col, int price) {
        this.row = row;
        this.col = col;
        this.price = price;
    }

    public int getCol() {
        return col;
    }

    public int getPrice() {
        return price;
    }

    public int getRow() {
        return row;
    }
}

class Room {
    @JsonProperty("total_rows")
    private final int totalRows;
    @JsonProperty("total_columns")
    private final int totalCols;
    @JsonProperty("available_seats")
    private List<Seat> availableSeats;

    public Room() {
        totalRows = 9;
        totalCols = 9;
        availableSeats = new ArrayList<Seat>();
        for (int i = 1; i <= 9; i++) {
            for (int j = 1; j <= 9; j++) {
                if (i <= 4) {
                    availableSeats.add(new Seat(i, j, 10));
                } else {
                    availableSeats.add(new Seat(i, j, 8));
                }
            }
        }
    }

    public void addSeat(Seat seat) {
        availableSeats.add(seat);
    }

    public Seat getSeat(int row, int col) {
        for (Seat each : availableSeats) {
            if ((each.getRow() == row) && (each.getCol() == col)) {
                return each;
            }
        }
        return null;
    }

    public void removeSeat(int row, int col) {
        for (int i = 0; i < availableSeats.size(); i++) {
            Seat seat = availableSeats.get(i);
            if ((seat.getRow() == row) && (seat.getCol() == col)) {
                availableSeats.remove(i);
                break;
            }
        }
    }
}