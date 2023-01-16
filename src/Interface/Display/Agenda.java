package Interface.Display;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

public  class Agenda {

    public SimpleStringProperty hours;
    public SimpleStringProperty monday;
    public SimpleStringProperty tuesday;
    public SimpleStringProperty wednesday;
    public SimpleStringProperty thursday;
    public SimpleStringProperty friday;
    public SimpleStringProperty saturday;
    public SimpleStringProperty sunday;


    public Agenda(String hours, String monday, String tuesday, String wednesday, String thursday, String friday, String saturday, String sunday) {

        this.hours = new SimpleStringProperty(hours);
        this.monday = new SimpleStringProperty(monday);
        this.tuesday = new SimpleStringProperty(tuesday);
        this.wednesday = new SimpleStringProperty(wednesday);
        this.thursday = new SimpleStringProperty(thursday);
        this.friday = new SimpleStringProperty(friday);
        this.saturday = new SimpleStringProperty(saturday);
        this.sunday = new SimpleStringProperty(sunday);
    }


    public String getHours() {
        return hours.get();
    }

    public void setHours(String fhours) {
        hours.set(fhours);
    }

    public String getMonday() {
        return monday.get();
    }

    public void setMonday(String fmonday) {
        monday.set(fmonday);
    }

    public String getTuesday() {
        return tuesday.get();
    }

    public void setTuesday(String ftuesday) {
        tuesday.set(ftuesday);
    }

    public String getWednesday() {
        return wednesday.get();
    }

    public void setWednesday(String fwednesday) {
        wednesday.set(fwednesday);
    }

    public String getThursday() {
        return thursday.get();
    }

    public void setThursday(String fthursday) {
        thursday.set(fthursday);
    }

    public String getFriday() {
        return friday.get();
    }

    public void setFriday(String ffriday) {
        friday.set(ffriday);
    }

    public String getSaturday() {
        return saturday.get();
    }

    public void setSaturday(String fsaturday) {
        saturday.set(fsaturday);
    }

    public String getSunday() {
        return sunday.get();
    }

    public void setSunday(String fsunday) {
        sunday.set(fsunday);
    }

    static class EditingCell extends TableCell<Agenda, String> {

        private TextField textField;

        public EditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText((String) getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> arg0,
                                    Boolean arg1, Boolean arg2) {
                    if (!arg2) {
                        commitEdit(textField.getText());
                    }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }


}