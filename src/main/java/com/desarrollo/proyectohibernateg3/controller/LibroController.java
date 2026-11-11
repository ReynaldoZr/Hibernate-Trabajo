package com.desarrollo.proyectohibernateg3.controller;

import javafx.scene.control.*;
import com.desarrollo.proyectohibernateg3.dao.LibroDAO;
import com.desarrollo.proyectohibernateg3.dao.CategoriaDAO;
import com.desarrollo.proyectohibernateg3.model.Libro;
import com.desarrollo.proyectohibernateg3.model.Categoria;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

public class LibroController {
    private final LibroDAO libroDAO = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private Long idSeleccionado;

    @FXML
    private TextField txtTitulo;
    @FXML
    private TextField txtAutor;
    @FXML
    private TextField txtAnio;
    @FXML
    private TextField txtPrecio;

    @FXML
    private ComboBox<Categoria> cmbCategoria;

    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnCargar;
    @FXML
    private Button btnModificar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Libro> tablaLibros;
    @FXML
    private TableColumn<Libro, Long> colId;
    @FXML
    private TableColumn<Libro, String> colTitulo;
    @FXML
    private TableColumn<Libro, String> colAutor;
    @FXML
    private TableColumn<Libro, Integer> colAnio;
    @FXML
    private TableColumn<Libro, BigDecimal> colPrecio;

    @FXML
    private TableColumn<Libro, String> colCategoria;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anioPublicacion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        colCategoria.setCellValueFactory(data -> {
            Categoria categoria = data.getValue().getCategoria();
            String nombre = categoria != null
                    ? categoria.getNombre()
                    : "Sin categoría";
            return new SimpleStringProperty(nombre);
        });

        tablaLibros.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        seleccionarLibro(newValue);
                    } else {
                        idSeleccionado = null;
                        actualizarBotones();
                    }
                }
        );

        actualizarBotones();
        cargarCategorias();
        cargarLibros();
    }

    private void seleccionarLibro(Libro libro) {
        idSeleccionado = libro.getId();
        txtTitulo.setText(libro.getTitulo());
        txtAutor.setText(libro.getAutor());
        txtAnio.setText(String.valueOf(libro.getAnioPublicacion()));
        txtPrecio.setText(libro.getPrecio().toPlainString());
        cmbCategoria.setValue(null);
        for (Categoria categoria : cmbCategoria.getItems()) {
            if (categoria.getId().equals(libro.getCategoria().getId())) {
                cmbCategoria.setValue(categoria);
                break;
            }
        }
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean sinSeleccion = idSeleccionado == null;
        btnModificar.setDisable(sinSeleccion);
        btnEliminar.setDisable(sinSeleccion);
    }

    private Libro obtenerLibroFormulario() {
        String titulo = txtTitulo.getText().trim();
        String autor = txtAutor.getText().trim();
        if (titulo.isBlank() || autor.isBlank()
                || txtAnio.getText().isBlank() || txtPrecio.getText().isBlank()) {
            throw new IllegalArgumentException("Completa todos los campos.");
        }
        if (titulo.length() > 150 || autor.length() > 100) {
            throw new IllegalArgumentException("El título admite 150 caracteres y el autor 100.");
        }
        int anio = Integer.parseInt(txtAnio.getText().trim());
        BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
        if (anio < 1450 || anio > 2100 || precio.signum() < 0) {
            throw new IllegalArgumentException("El año debe estar entre 1450 y 2100 y el precio no puede ser negativo.");
        }

        Categoria categoria = cmbCategoria.getValue();
        if (categoria == null) {
            throw new IllegalArgumentException("Selecciona una categoría.");
        }
        Libro libro = new Libro(titulo, autor, anio, precio, categoria);
        libro.setId(idSeleccionado);
        return libro;
    }

    @FXML
    private void guardarLibro() {
        try {
            Libro libro = obtenerLibroFormulario();
            libro.setId(null);
            libroDAO.guardar(libro);
            limpiarCampos();
            cargarLibros();
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError(ex);
        }
    }
    @FXML
    private void modificarLibro() {
        try {
            if (idSeleccionado == null) {
                throw new IllegalArgumentException("Selecciona un libro para modificar.");
            }
            Libro libro = obtenerLibroFormulario();
            libro.setId(idSeleccionado);
            boolean modificado = libroDAO.modificar(libro);
            if (!modificado) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText("Libro no encontrado");
                alert.showAndWait();
            }

            limpiarCampos();
            cargarLibros();
        }catch (Exception ex){
            ex.printStackTrace();
            mostrarError(ex);
        }
    }
    @FXML
    private void eliminarLibro() {
        try {
            if (idSeleccionado == null) {
                throw new IllegalArgumentException("Selecciona un libro para eliminar.");
            }
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmacion de eliminacion de datos");
            confirmacion.setHeaderText("Eliminar libro");
            confirmacion.setContentText("¿Está seguro que desea eliminar el registro seleccionado?");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if(resultado.isEmpty() || resultado.get() != ButtonType.OK){
                return;
            }

            boolean eliminado = libroDAO.eliminar(idSeleccionado);
            if (!eliminado) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText("Libro no encontrado");
                alert.showAndWait();

            }
            limpiarCampos();
            cargarLibros();
        }catch (Exception ex){
            ex.printStackTrace();
            mostrarError(ex);
        }
    }

    @FXML
    private void limpiarCampos() {
        tablaLibros.getSelectionModel().clearSelection();
        idSeleccionado = null;
        txtTitulo.clear();
        txtAutor.clear();
        txtAnio.clear();
        txtPrecio.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        cmbCategoria.setValue(null);
        actualizarBotones();
    }

    @FXML
    private void cargarLibros() {
        try {
            List<Libro> libros = libroDAO.listar();
            tablaLibros.setItems(FXCollections.observableList(libros));
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError(ex);
        }
    }

    private void cargarCategorias() {
        try {
            List<Categoria> categorias = categoriaDAO.listar();
            cmbCategoria.setItems(
                    FXCollections.observableArrayList(categorias)
            );
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudieron cargar las categorías.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(Exception ex) {
        String mensaje = ex instanceof NumberFormatException
                ? "Introduce un año entero y un precio numérico válido."
                : ex instanceof IllegalArgumentException
                    ? ex.getMessage()
                    : "No se pudo completar la operación. Revisa la conexión a la base de datos.";
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
