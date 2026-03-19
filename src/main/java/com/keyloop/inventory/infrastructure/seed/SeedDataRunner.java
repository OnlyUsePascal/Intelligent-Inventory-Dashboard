package com.keyloop.inventory.infrastructure.seed;

import com.keyloop.inventory.domain.model.Employee;
import com.keyloop.inventory.domain.model.EmployeeRole;
import com.keyloop.inventory.domain.model.InventoryType;
import com.keyloop.inventory.domain.model.Reservation;
import com.keyloop.inventory.domain.model.ReservationStatus;
import com.keyloop.inventory.domain.model.Tenant;
import com.keyloop.inventory.domain.model.Vehicle;
import com.keyloop.inventory.domain.model.VehicleAction;
import com.keyloop.inventory.domain.model.VehicleStatus;
import com.keyloop.inventory.domain.repository.EmployeeRepository;
import com.keyloop.inventory.domain.repository.ReservationRepository;
import com.keyloop.inventory.domain.repository.TenantRepository;
import com.keyloop.inventory.domain.repository.VehicleActionRepository;
import com.keyloop.inventory.domain.repository.VehicleRepository;
import com.keyloop.inventory.infrastructure.persistence.repository.SpringDataEmployeeRepository;
import com.keyloop.inventory.infrastructure.persistence.repository.SpringDataReservationRepository;
import com.keyloop.inventory.infrastructure.persistence.repository.SpringDataTenantRepository;
import com.keyloop.inventory.infrastructure.persistence.repository.SpringDataVehicleActionRepository;
import com.keyloop.inventory.infrastructure.persistence.repository.SpringDataVehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SeedDataRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(SeedDataRunner.class);

    private final Environment environment;
    private final TenantRepository tenantRepository;
    private final EmployeeRepository employeeRepository;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final VehicleActionRepository vehicleActionRepository;
    private final SpringDataVehicleActionRepository springVehicleActionRepository;
    private final SpringDataReservationRepository springReservationRepository;
    private final SpringDataVehicleRepository springVehicleRepository;
    private final SpringDataEmployeeRepository springEmployeeRepository;
    private final SpringDataTenantRepository springTenantRepository;

    public SeedDataRunner(Environment environment,
                          TenantRepository tenantRepository,
                          EmployeeRepository employeeRepository,
                          VehicleRepository vehicleRepository,
                          ReservationRepository reservationRepository,
                          VehicleActionRepository vehicleActionRepository,
                          SpringDataVehicleActionRepository springVehicleActionRepository,
                          SpringDataReservationRepository springReservationRepository,
                          SpringDataVehicleRepository springVehicleRepository,
                          SpringDataEmployeeRepository springEmployeeRepository,
                          SpringDataTenantRepository springTenantRepository) {
        this.environment = environment;
        this.tenantRepository = tenantRepository;
        this.employeeRepository = employeeRepository;
        this.vehicleRepository = vehicleRepository;
        this.reservationRepository = reservationRepository;
        this.vehicleActionRepository = vehicleActionRepository;
        this.springVehicleActionRepository = springVehicleActionRepository;
        this.springReservationRepository = springReservationRepository;
        this.springVehicleRepository = springVehicleRepository;
        this.springEmployeeRepository = springEmployeeRepository;
        this.springTenantRepository = springTenantRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!isSeedEnabled()) {
            logger.info("SEED_DATA is not true. Skipping seed process.");
            return;
        }

        logger.info("SEED_DATA=true detected. Starting seed process.");
        purgeData();
        SeedSummary summary = seedData();
        logger.info("Seed complete. Tenants={}, Employees={}, Vehicles={}, Reservations={}, Actions={}",
                summary.tenants, summary.employees, summary.vehicles, summary.reservations, summary.actions);
    }

    private boolean isSeedEnabled() {
        String raw = environment.getProperty("SEED_DATA", "false");
        return Boolean.parseBoolean(raw);
    }

    private void purgeData() {
        logger.info("Purging existing data in order: vehicle_action -> reservation -> vehicle -> employee -> tenant");
        springVehicleActionRepository.deleteAllInBatch();
        springReservationRepository.deleteAllInBatch();
        springVehicleRepository.deleteAllInBatch();
        springEmployeeRepository.deleteAllInBatch();
        springTenantRepository.deleteAllInBatch();
    }

    private SeedSummary seedData() {
        Tenant tenantPrimary = tenantRepository.save(Tenant.builder()
                .name("Keyloop City Motors")
                .build());
        Tenant tenantSecondary = tenantRepository.save(Tenant.builder()
                .name("Keyloop Lakeside Auto")
                .build());

        Employee admin = employeeRepository.save(Employee.builder()
                .tenantId(tenantPrimary.getId())
                .name("Avery Morgan")
                .role(EmployeeRole.ADMIN)
                .build());
        Employee inventory = employeeRepository.save(Employee.builder()
                .tenantId(tenantPrimary.getId())
                .name("Casey Nguyen")
                .role(EmployeeRole.INVENTORY)
                .build());
        Employee sales = employeeRepository.save(Employee.builder()
                .tenantId(tenantPrimary.getId())
                .name("Jordan Patel")
                .role(EmployeeRole.SALE)
                .build());
        employeeRepository.save(Employee.builder()
                .tenantId(tenantSecondary.getId())
                .name("Morgan Lee")
                .role(EmployeeRole.ADMIN)
                .build());

        List<Vehicle> vehicles = buildVehicles(tenantPrimary.getId());
        List<Vehicle> savedVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            savedVehicles.add(vehicleRepository.save(vehicle));
        }

        List<Reservation> reservations = buildReservations(savedVehicles, sales.getId());
        for (Reservation reservation : reservations) {
            reservationRepository.save(reservation);
        }

        List<VehicleAction> actions = buildActions(savedVehicles, inventory.getId(), admin.getId());
        for (VehicleAction action : actions) {
            vehicleActionRepository.save(action);
        }

        return new SeedSummary(2, 4, savedVehicles.size(), reservations.size(), actions.size());
    }

    private List<Vehicle> buildVehicles(UUID tenantId) {
        String[] makes = {"Toyota", "Honda", "Ford", "BMW", "Audi", "Nissan", "Hyundai", "Kia", "Volkswagen", "Mazda"};
        String[] models = {"Corolla", "Civic", "Focus", "X3", "A4", "Altima", "Tucson", "Sportage", "Golf", "CX-5"};
        VehicleStatus[] statuses = {
                VehicleStatus.AVAILABLE,
                VehicleStatus.AVAILABLE,
                VehicleStatus.AVAILABLE,
                VehicleStatus.RESERVED,
                VehicleStatus.RESERVED,
                VehicleStatus.SOLD,
                VehicleStatus.SOLD,
                VehicleStatus.UNAVAILABLE,
                VehicleStatus.AVAILABLE,
                VehicleStatus.RESERVED,
                VehicleStatus.AVAILABLE,
                VehicleStatus.SOLD,
                VehicleStatus.UNAVAILABLE,
                VehicleStatus.AVAILABLE,
                VehicleStatus.AVAILABLE,
                VehicleStatus.RESERVED,
                VehicleStatus.SOLD,
                VehicleStatus.AVAILABLE,
                VehicleStatus.AVAILABLE,
                VehicleStatus.UNAVAILABLE
        };
        InventoryType[] types = {
                InventoryType.USED,
                InventoryType.NEW,
                InventoryType.USED,
                InventoryType.DEMO,
                InventoryType.USED,
                InventoryType.USED,
                InventoryType.NEW,
                InventoryType.USED,
                InventoryType.DEMO,
                InventoryType.NEW,
                InventoryType.USED,
                InventoryType.USED,
                InventoryType.USED,
                InventoryType.NEW,
                InventoryType.DEMO,
                InventoryType.USED,
                InventoryType.USED,
                InventoryType.NEW,
                InventoryType.USED,
                InventoryType.DEMO
        };
        int[] daysInInventory = {15, 35, 120, 75, 95, 140, 20, 50, 10, 110, 25, 200, 5, 60, 30, 100, 45, 160, 80, 12};

        LocalDate today = LocalDate.now();
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String vin = String.format("1HGCM82633A%06d", i + 1);
            String licensePlate = String.format("KLP-%03d", 101 + i);
            int year = 2018 + (i % 7);
            int mileage = 5000 + (i * 1200);
            LocalDate receivedDate = today.minusDays(daysInInventory[i]);
            LocalDate availableForSaleDate = receivedDate.plusDays(7);

            vehicles.add(buildVehicle(tenantId, vin, licensePlate, makes[i % makes.length],
                    models[i % models.length], year, mileage, statuses[i], types[i],
                    receivedDate, availableForSaleDate));
        }

        return vehicles;
    }

    private Vehicle buildVehicle(UUID tenantId, String vin, String licensePlate, String make,
                                 String model, int year, int mileage, VehicleStatus status,
                                 InventoryType inventoryType, LocalDate receivedDate,
                                 LocalDate availableForSaleDate) {
        return Vehicle.builder()
                .tenantId(tenantId)
                .vin(vin)
                .licensePlate(licensePlate)
                .make(make)
                .model(model)
                .year(year)
                .mileage(mileage)
                .status(status)
                .inventoryType(inventoryType)
                .receivedDate(receivedDate)
                .availableForSaleDate(availableForSaleDate)
                .build();
    }

    private List<Reservation> buildReservations(List<Vehicle> vehicles, UUID employeeId) {
        Instant now = Instant.now();
        List<Reservation> reservations = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            if (vehicle.getStatus() != VehicleStatus.RESERVED) {
                continue;
            }

            reservations.add(Reservation.builder()
                    .vehicleId(vehicle.getId())
                    .employeeId(employeeId)
                    .reservationDate(now.minusSeconds(3600))
                    .reservedUntilDate(now.plusSeconds(86400 * 7L))
                    .status(ReservationStatus.ACTIVE)
                    .build());
        }

        return reservations;
    }

    private List<VehicleAction> buildActions(List<Vehicle> vehicles, UUID inventoryId, UUID adminId) {
        Instant now = Instant.now();
        List<VehicleAction> actions = new ArrayList<>();

        if (vehicles.size() < 5) {
            return actions;
        }

        actions.add(action(vehicles.get(0).getId(), inventoryId,
                "Inbound inspection completed; minor scuff noted on rear bumper.", now.minusSeconds(7200)));
        actions.add(action(vehicles.get(3).getId(), adminId,
                "Price adjusted after market review.", now.minusSeconds(5400)));
        actions.add(action(vehicles.get(5).getId(), inventoryId,
                "Service history verified and uploaded.", now.minusSeconds(3600)));
        actions.add(action(vehicles.get(9).getId(), adminId,
                "Hold placed for upcoming customer test drive.", now.minusSeconds(1800)));
        actions.add(action(vehicles.get(12).getId(), inventoryId,
                "Vehicle moved to overflow lot pending detailing.", now.minusSeconds(900)));

        return actions;
    }

    private VehicleAction action(UUID vehicleId, UUID employeeId, String text, Instant timestamp) {
        return VehicleAction.builder()
                .vehicleId(vehicleId)
                .employeeId(employeeId)
                .actionText(text)
                .timestamp(timestamp)
                .build();
    }

    private static class SeedSummary {
        private final int tenants;
        private final int employees;
        private final int vehicles;
        private final int reservations;
        private final int actions;

        private SeedSummary(int tenants, int employees, int vehicles, int reservations, int actions) {
            this.tenants = tenants;
            this.employees = employees;
            this.vehicles = vehicles;
            this.reservations = reservations;
            this.actions = actions;
        }
    }
}
