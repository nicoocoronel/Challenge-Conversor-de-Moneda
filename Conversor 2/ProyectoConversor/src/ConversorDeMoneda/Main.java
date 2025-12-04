package ConversorDeMoneda;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    //Crea un mapa de monedas con su simbolo
    private static final Map<Integer, String> MONEDAS = new HashMap<>();
    //archivo para guardar las conversiones
    private static final String ARCHIVO_CONVERSIONES = "conversiones.txt";

    static {
        MONEDAS.put(1, "USD"); //dolar estadounidense
        MONEDAS.put(2, "EUR"); //euro
        MONEDAS.put(3, "JPY");//yen japones
        MONEDAS.put(4, "GBP");//libra esterlina
        MONEDAS.put(5, "AUD");//dolar australiano
        MONEDAS.put(6, "CAD");//dolar canadiense
        MONEDAS.put(7, "ARS");//peso argentino
        MONEDAS.put(8, "BRL");//reales
    }

    public static void main(String[] args) {
        Configuracion configuracion = new Configuracion();
        String apiKey = configuracion.getProperty("api.exchangerate.key");

        Scanner scanner = new Scanner(System.in);
        ClienteHTTP clienteHTTP = new ClienteHTTP();

        while (true) {
            System.out.println("\\n==================================");
            System.out.println("====== Conversor de Monedas ======");
            System.out.println("==================================");
            System.out.println("\n1. Convertir importe");
            System.out.println("n2. Ver valores de cotizacion");
            System.out.println("n3. Ver conversiones anteriores");
            System.out.println("n4. Salir");

            int opcion = 0;

            //Intentar capturar una opcion valida del menu
            while (true) {
                try {
                    System.out.println("\nEscoja una opcion: ");
                    opcion = Integer.parseInt(scanner.nextLine());
                    if (opcion < 1 || opcion > 4) {
                        System.out.println("Opcion invalida, por favor ingrese un numero de 1 al 4.");
                    } else {
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Entrada invalida. Por favor ingresar un numero entero.");
                }
            }

            String baseMoneda = "";
            JsonObject rates = null;

            if (opcion == 1 || opcion == 2) {
                System.out.println("\nMonedas dispoibles: ");
                mostrarMonedas();
                //Intentar capturar una moneda valida
                while (true) {
                    try {
                        System.out.println("\nSeleccione la moneda base (numero entero):");
                        int monedaSeleccionada = Integer.parseInt(scanner.nextLine());
                        baseMoneda = MONEDAS.get(monedaSeleccionada);
                        if (baseMoneda != null) {
                            break;
                        } else {
                            System.out.println("Moneda invalida. Ingresa un numero del 1 al 8.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Entrada invalida. Por favor ingrese un numero entero.");
                    }
                }

                //Realizar solicitud para obtener los datos de la API
                String datosJSON = clienteHTTP.obtenerDatos(apiKey,baseMoneda);
                JsonObject jsonObject = JsonParser.parseString(datosJSON).getAsJsonObject();
                rates = jsonObject.getAsJsonObject("conversion_rates");
            }

            switch (opcion) {
                case 1:
                    convertirMoneda(scanner , rates, baseMoneda);
                    break;
                case 2:
                    verValoresDeCotizacion(scanner, rates);
                    break;
                case 3:
                    verConversionesAnteriores();
                    break;
                case 4:
                    System.out.println("\nSaliendo...");
                    return;
            }
        }
    }
    //Metodo para mostrar las monedas disponibles
    private static void mostrarMonedas(){
        System.out.println("1. USD       5. AUD");
        System.out.println("2. EUR       6. CAD");
        System.out.println("3. JPY       7. ARS");
        System.out.println("4. GBP       8. BRL");
    }
    //Metodo para convertir Moneda
    public static void convertirMoneda(Scanner scanner, JsonObject rates, String baseMoneda) {
        String monedaObjetivo = "";

        //capturar la moneda valida
        while (true) {
            try {
                System.out.println("Seleccione la moneda que desea convertir (numero entero): ");
                int monedaObjetivoSeleccionada = Integer.parseInt(scanner.nextLine());
                monedaObjetivo = MONEDAS.get(monedaObjetivoSeleccionada);
                if (monedaObjetivo != null) {
                    break;
                } else {
                    System.out.println("Moneda invalida. Selecciona un numero entre 1 y 8.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida. Por favor ingrese un numero entero");
            }
        }

        int importe = 0;
        //intentar capturar un importe valido
        while (true) {
            try {
                System.out.println("Ingrese el importe a convertir (numero entero): ");
                importe = Integer.parseInt(scanner.nextLine());
                if(importe > 0) {
                    break;
                } else {
                    System.out.println("El importe debe ser mayore a 0(cero)");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida. Por favor ingrese un numero entero.");
        }
    }

        //verificar si la moneda objetivo existe en la tasa de conversion
        if (rates.has(monedaObjetivo)) {
            double tasaDeConversion = rates.get(monedaObjetivo).getAsDouble();
            double tasaCompra = tasaDeConversion * 0.99;
            double tasaVenta = tasaDeConversion * 1.01;

            double importeConvertidoCompra = importe * tasaCompra;
            double importeConvertidoVenta = importe * tasaVenta;

            Moneda conversion = new Moneda(baseMoneda, monedaObjetivo, importe, importeConvertidoCompra, importeConvertidoVenta);
            guardarConversion(conversion);
            //Mostrar el resultado
            System.out.println("\nResultado de la conversion");
            System.out.println("| De -> A      | Importe Base   | Valor de Compra | Valor de Venta  |");
            System.out.println("|--------------|----------------|-----------------|-----------------|");
            System.out.println(conversion.formatoMoneda());
        } else {
            System.out.println("\nLa moneda ingresada no esta disponible en las tasas de conversion.");
        }
}

//metodo para ver los valores de cotizacion de una moneda especifica
public static void verValoresDeCotizacion(Scanner scanner, JsonObject rates) {
    String moneda = "";
    //capturar moneda valida
    while (true) {
        try {
            System.out.println("Seleccione la moneda de la cual desea ver la cotizacion (numero entero).");
            int monedaSeleccionada = Integer.parseInt(scanner.nextLine());
            moneda = MONEDAS.get(monedaSeleccionada);
            if (moneda != null) {
                break;
            } else {
                System.out.println("Moneda invalida. Seleccione un numero del 1 al 8.");
                }
            } catch (NumberFormatException e) {
            System.out.println("Moneda invalida. Por favor ingrese un numero entero.");
            }
        }

    //Verificar si la moneda existe en la tasa de conversion
             if (rates.has(moneda)) {
                 double tasaConversion = rates.get(moneda).getAsDouble();
                 double tasaCompra = tasaConversion * 0.99;
                 double tasaVenta = tasaConversion * 1.01;
                 // Mostrar cotizaciones de compra y venta
        System.out.println("\nCotizaciones para " + moneda + ":");
        System.out.println("\n| Moneda     | Valor de Compra | Valor de Venta  |");
        System.out.println("|------------|-----------------|-----------------|");
        System.out.printf("| %-10s | %-15.4f | %-15.4f |\n", moneda, tasaCompra, tasaVenta);
        } else {
            System.out.println("\nLa moneda ingresada no está disponible.");
        }
}

//Metodo para guardar la conversion en un archivo
    public static void guardarConversion(Moneda conversion) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_CONVERSIONES, true))) {
            writer.write(conversion.formatoMoneda());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("\nError al guardar conversion: " + e.getMessage());
        }
    }
    //Metodo para ver las conversiones anteriores
    public static void verConversionesAnteriores() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_CONVERSIONES))) {
            String linea;
            System.out.println("\n=== Conversiones Anteriores ===");
            while ((linea = reader.readLine()) != null) {
                System.out.println(linea);
            }
        } catch (IOException e) {
            System.out.println("\nError al leer el archivo de conversiolnes: " + e.getMessage());
        }
    }
}
