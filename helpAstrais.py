#!/usr/bin/env python3
import tkinter as tk
from tkinter import ttk, messagebox, scrolledtext, filedialog
import subprocess
import os
import sys
import re
from pathlib import Path

# === CONFIGURACIÓN ASTRAIS ===
COMMIT_TYPES = [
    ("feat", "Nueva funcionalidad"),
    ("fix", "Correccion de bug"),
    ("refactor", "Mejora de codigo"),
    ("perf", "Optimizacion"),
    ("docs", "Documentacion"),
    ("test", "Tests"),
    ("chore", "Mantenimiento"),
    ("ci", "CI/CD"),
    ("style", "Estilo"),
    ("revert", "Reversion")
]
PREDEFINED_SCOPES = ["auth", "tasks", "groups", "economy", "shop", "achievements",
                     "avatar", "pet", "minigames", "calendar", "android", "web",
                     "backend", "db", "docker"]
MAX_SUBJECT = 72


class GitCommitHelper:
    def __init__(self, root):
        self.root = root
        self.root.title("Astrais Git Helper: add -> commit -> push")
        self.root.geometry("800x750")
        self.root.resizable(False, False)
        self.repo_path = Path.cwd()
        self.validate_git_env()
        self.create_widgets()
        self.refresh_git_status()

    def check_remote_changes(self, rama="origin/main"):
        # Captura el numero de commits de diferencia entre la rama local y la indicada (principal por defecto)
        try:
            result = subprocess.run(
                ["git", "rev-list", "--left-right", "--count", "HEAD..."+rama],
                capture_output=True, text=True, cwd=self.repo_path, timeout=10
            )
            if result.returncode == 0:
                self.git_remote_changes = result.stdout.strip().split("\t")
            else:
                self.git_remote_changes = ["0", "0"]
        except Exception:
            self.git_remote_changes = ["0", "0"]

    def validate_git_env(self):
        try:
            result = subprocess.run(
                ["git", "rev-parse", "--git-dir"],
                capture_output=True, text=True, cwd=self.repo_path, timeout=5
            )
            if result.returncode != 0:
                raise Exception("No es un repositorio Git valido")
        except Exception as e:
            messagebox.showerror("Error critico",
                f"No se detecto repositorio Git:\n{str(e)}\n\n"
                f"Ruta actual: {self.repo_path}\n\n"
                "Ejecuta el script desde la carpeta raiz de tu proyecto.")
            sys.exit(1)

    def refresh_git_status(self):
        """Obtiene estado actual de Git (staged/unstaged)"""
        try:
            staged = subprocess.run(
                ["git", "diff", "--cached", "--name-only"],
                capture_output=True, text=True, cwd=self.repo_path, timeout=3
            ).stdout.strip().splitlines()

            unstaged = subprocess.run(
                ["git", "diff", "--name-only"],
                capture_output=True, text=True, cwd=self.repo_path, timeout=3
            ).stdout.strip().splitlines()

            untracked = subprocess.run(
                ["git", "ls-files", "--others", "--exclude-standard"],
                capture_output=True, text=True, cwd=self.repo_path, timeout=3
            ).stdout.strip().splitlines()

            self.staged_files = [f for f in staged if f]
            self.unstaged_files = [f for f in unstaged + untracked if f]
            self.current_branch = subprocess.run(
                ["git", "rev-parse", "--abbrev-ref", "HEAD"],
                capture_output=True, text=True, cwd=self.repo_path, timeout=2
            ).stdout.strip() or "main"

            self.check_remote_changes(f"origin/{self.current_branch}")
            self.update_status_display()

        except Exception as e:
            self.status_var.set(f"Advertencia: Error al obtener estado: {str(e)[:50]}")
            self.staged_files = []
            self.unstaged_files = []
            self.current_branch = "main"
       
        if self.scope_combo.cget("state") == "readonly":
            suggested = self.suggest_scope_from_files()
            self.scope_var.set(suggested)

    def update_status_display(self):
        for widget in self.status_frame.winfo_children():
            widget.destroy()

        branch_label = tk.Label(
            self.status_frame,
            text=f"Rama actual: {self.current_branch}",
            bg="#e3f2fd", fg="#1565c0", font=("Segoe UI", 10, "bold")
        )
        branch_label.pack(anchor="w", padx=10, pady=(5, 0))

        if self.unstaged_files:
            tk.Label(
                self.status_frame,
                text=f"{len(self.unstaged_files)} archivos sin stagear:",
                bg="#e3f2fd", fg="#d32f2f", font=("Segoe UI", 9, "bold")
            ).pack(anchor="w", padx=10, pady=(5, 0))

            files_text = "\n".join(f"  - {f[:40]}..." if len(f) > 40 else f"  - {f}"
                                 for f in self.unstaged_files[:5])
            if len(self.unstaged_files) > 5:
                files_text += f"\n  - ... y {len(self.unstaged_files)-5} mas"

            tk.Label(
                self.status_frame,
                text=files_text,
                bg="#e3f2fd", fg="#546e7a", font=("Segoe UI", 8),
                justify="left", anchor="w"
            ).pack(anchor="w", padx=20, pady=(0, 5))

            tk.Button(
                self.status_frame,
                text="Stage all changes",
                command=self.stage_all_changes,
                bg="#1976d2", fg="white", font=("Segoe UI", 9, "bold"),
                padx=10, pady=3, relief="flat", cursor="hand2"
            ).pack(anchor="w", padx=20, pady=(0, 10))
            tk.Button(
                self.status_frame,
                text="Stage single file",
                command=self.stage_single_file,
                bg="#455a64",
                fg="white",
                font=("Segoe UI", 9),
                padx=10,
                pady=3,
                relief="flat",
                cursor="hand2"
            ).pack(anchor="w", padx=20, pady=(0, 10))

        else:
            tk.Label(
                self.status_frame,
                text="Sin cambios sin stagear",
                bg="#e3f2fd", fg="#2e7d32", font=("Segoe UI", 9)
            ).pack(anchor="w", padx=10, pady=5)

        if self.staged_files:
            tk.Label(
                self.status_frame,
                text=f"{len(self.staged_files)} archivos staged:",
                bg="#e3f2fd", fg="#2e7d32", font=("Segoe UI", 9, "bold")
            ).pack(anchor="w", padx=10, pady=(5, 0))

            files_text = "\n".join(f"  - {f[:40]}..." if len(f) > 40 else f"  - {f}"
                                 for f in self.staged_files[:5])
            if len(self.staged_files) > 5:
                files_text += f"\n  - ... y {len(self.staged_files)-5} mas"

            tk.Label(
                self.status_frame,
                text=files_text,
                bg="#e3f2fd", fg="#546e7a", font=("Segoe UI", 8),
                justify="left", anchor="w"
            ).pack(anchor="w", padx=20, pady=(0, 5))
            tk.Button(
                self.status_frame,
                text="Unstage single file",
                command=self.unstage_single_file,
                bg="#c62828",
                fg="white",
                font=("Segoe UI", 9),
                padx=10,
                pady=3,
                relief="flat",
                cursor="hand2"
            ).pack(anchor="w", padx=20, pady=(0, 10))
        else:
            tk.Label(
                self.status_frame,
                text="No hay archivos staged (necesario para commit)",
                bg="#e3f2fd", fg="#ed6c02", font=("Segoe UI", 9)
            ).pack(anchor="w", padx=10, pady=5)

        # Cambio: numero de commits.
        local_commits = int(self.git_remote_changes[0])
        remote_commits = int(self.git_remote_changes[1])
        if local_commits == 0 and remote_commits == 0:
            text = "Estás en sintonía con origin/master"
            fg = "#02ed29"

        elif local_commits > 0 and remote_commits == 0:
            text = f"Vas {local_commits} commits por delante de origin/master"
            fg = "#8b501f"

        elif local_commits == 0 and remote_commits > 0:
            text = f"Rama desactualizada: {remote_commits} commits detrás de origin/master"
            fg = "#ed1202"

        else:
            text = (
                "La rama ha divergido del remoto\n"
                f"Local: {local_commits} | Remoto: {remote_commits}\n"
                "Recomendado: pull --rebase"
            )
            fg = "#ff9800"

        tk.Label(
            self.status_frame,
            text=text,
            bg="#e3f2fd",
            fg=fg,
            font=("Segoe UI", 9, "bold"),
            justify="left"
        ).pack(anchor="w", padx=10, pady=5)


    def stage_all_changes(self):
        if not self.unstaged_files:
            messagebox.showinfo("Informacion", "No hay cambios para stagear")
            return

        msg = f"Stagear {len(self.unstaged_files)} archivos?\n\n"
        msg += "\n".join(f"  - {f}" for f in self.unstaged_files[:8])
        if len(self.unstaged_files) > 8:
            msg += f"\n  - ... y {len(self.unstaged_files)-8} mas"

        if not messagebox.askyesno("Stage changes", msg):
            return

        try:
            result = subprocess.run(
                ["git", "add", "."],
                capture_output=True, text=True, cwd=self.repo_path, timeout=10
            )
            if result.returncode == 0:
                messagebox.showinfo("Exito", f"Staged {len(self.unstaged_files)} archivos")
                self.refresh_git_status()
            else:
                raise Exception(result.stderr.strip() or "Error desconocido al stagear")
        except Exception as e:
            messagebox.showerror("Error", f"No se pudieron stagear los cambios:\n{str(e)}")
    
    def stage_single_file(self):
        """Permite stagear un único archivo seleccionado."""
        if not self.unstaged_files:
            messagebox.showinfo("Informacion", "No hay archivos sin stagear")
            return

        # Ventana simple de selección
        selector = tk.Toplevel(self.root)
        selector.title("Stagear archivo")
        selector.geometry("500x400")
        selector.resizable(False, False)

        tk.Label(selector, text="Selecciona un archivo para stagear:",
                font=("Segoe UI", 10, "bold")).pack(pady=10)

        listbox = tk.Listbox(selector, width=70, height=15)
        listbox.pack(padx=10, pady=5)

        for f in self.unstaged_files:
            listbox.insert(tk.END, f)

        def confirm_stage():
            selection = listbox.curselection()
            if not selection:
                messagebox.showwarning("Advertencia", "Selecciona un archivo")
                return

            file_to_stage = listbox.get(selection[0])

            try:
                result = subprocess.run(
                    ["git", "add", file_to_stage],
                    capture_output=True, text=True,
                    cwd=self.repo_path, timeout=10
                )

                if result.returncode == 0:
                    messagebox.showinfo("Exito", f"Archivo stageado:\n{file_to_stage}")
                    selector.destroy()
                    self.refresh_git_status()
                else:
                    raise Exception(result.stderr.strip())

            except Exception as e:
                messagebox.showerror("Error", f"No se pudo stagear:\n{str(e)}")

        tk.Button(selector, text="Stagear",
                command=confirm_stage,
                bg="#1976d2", fg="white",
                font=("Segoe UI", 9, "bold"),
                padx=10, pady=5).pack(pady=10)

    def unstage_single_file(self):
        """Permite quitar un archivo del área de stage."""
        if not self.staged_files:
            messagebox.showinfo("Informacion", "No hay archivos stageados")
            return

        selector = tk.Toplevel(self.root)
        selector.title("Quitar archivo de stage")
        selector.geometry("500x400")
        selector.resizable(False, False)

        tk.Label(
            selector,
            text="Selecciona un archivo para quitar del stage:",
            font=("Segoe UI", 10, "bold")
        ).pack(pady=10)

        listbox = tk.Listbox(selector, width=70, height=15)
        listbox.pack(padx=10, pady=5)

        for f in self.staged_files:
            listbox.insert(tk.END, f)

        def confirm_unstage():
            selection = listbox.curselection()
            if not selection:
                messagebox.showwarning("Advertencia", "Selecciona un archivo")
                return

            file_to_unstage = listbox.get(selection[0])

            try:
                result = subprocess.run(
                    ["git", "restore", "--staged", file_to_unstage],
                    capture_output=True,
                    text=True,
                    cwd=self.repo_path,
                    timeout=10
                )

                # Fallback para versiones antiguas de git
                if result.returncode != 0:
                    result = subprocess.run(
                        ["git", "reset", "HEAD", file_to_unstage],
                        capture_output=True,
                        text=True,
                        cwd=self.repo_path,
                        timeout=10
                    )

                if result.returncode == 0:
                    messagebox.showinfo("Exito", f"Archivo quitado del stage:\n{file_to_unstage}")
                    selector.destroy()
                    self.refresh_git_status()
                else:
                    raise Exception(result.stderr.strip())

            except Exception as e:
                messagebox.showerror("Error", f"No se pudo quitar del stage:\n{str(e)}")

        tk.Button(
            selector,
            text="Quitar de stage",
            command=confirm_unstage,
            bg="#d32f2f",
            fg="white",
            font=("Segoe UI", 9, "bold"),
            padx=10,
            pady=5
        ).pack(pady=10)


    def change_repo_path(self):
        new_path = filedialog.askdirectory(initialdir=self.repo_path)
        if not new_path:
            return
        new_path = Path(new_path)
        try:
            result = subprocess.run(
                ["git", "rev-parse", "--git-dir"],
                capture_output=True, text=True, cwd=new_path, timeout=5
            )
            if result.returncode != 0:
                messagebox.showerror("Error", "La carpeta seleccionada no es un repositorio Git valido.")
                return
            self.repo_path = new_path
            self.refresh_git_status()
            messagebox.showinfo("Exito", f"Ruta cambiada a:\n{self.repo_path}")
        except Exception as e:
            messagebox.showerror("Error", f"No se pudo cambiar la ruta:\n{str(e)}")

    def create_widgets(self):
        top_frame = tk.Frame(self.root)
        top_frame.pack(fill="x", padx=15, pady=5)

        self.status_var = tk.StringVar()

        tk.Label(top_frame, text="Ruta del repositorio:", font=("Segoe UI", 9, "bold")).pack(side=tk.LEFT)
        self.path_label = tk.Label(top_frame, text=str(self.repo_path), fg="#1565c0", font=("Consolas", 9))
        self.path_label.pack(side=tk.LEFT, padx=(5, 10))
        tk.Button(top_frame, text="Cambiar...", command=self.change_repo_path,
                  bg="#6c757d", fg="white", font=("Segoe UI", 8), padx=8, pady=2).pack(side=tk.RIGHT)

        self.status_frame = tk.Frame(self.root, bg="#e3f2fd", height=220)
        self.status_frame.pack(fill="x", padx=15, pady=5)
        self.status_frame.pack_propagate(True)

        ttk.Separator(self.root, orient="horizontal").pack(fill="x", padx=15, pady=5)

        main_frame = tk.Frame(self.root)
        main_frame.pack(fill="both", expand=True, padx=20, pady=10)

        tk.Label(main_frame, text="Tipo:", font=("Segoe UI", 10, "bold")).grid(row=0, column=0, sticky="w", pady=5)
        self.type_var = tk.StringVar()
        type_combo = ttk.Combobox(main_frame, textvariable=self.type_var,
                                 values=[f"{t[0]} - {t[1]}" for t in COMMIT_TYPES],
                                 state="readonly", width=38)
        type_combo.grid(row=0, column=1, columnspan=2, sticky="w", pady=5)
        type_combo.current(0)

        tk.Label(main_frame, text="Ambito:", font=("Segoe UI", 10, "bold")).grid(row=1, column=0, sticky="w", pady=5)

        scope_frame = tk.Frame(main_frame)
        scope_frame.grid(row=1, column=1, columnspan=2, sticky="w", pady=5)

        self.scope_var = tk.StringVar(value="tasks")
        self.scope_combo = ttk.Combobox(
            scope_frame,
            textvariable=self.scope_var,
            values=PREDEFINED_SCOPES + ["Personalizar..."],
            state="readonly",
            width=22
        )
        self.scope_combo.pack(side=tk.LEFT)
        self.scope_combo.bind("<<ComboboxSelected>>", self.on_scope_selected)

        tk.Button(
            scope_frame,
            text="Editar",
            command=self.enable_custom_scope,
            bg="#e0e0e0",
            width=6,
            relief="flat",
            cursor="hand2",
            font=("Segoe UI", 9)
        ).pack(side=tk.LEFT, padx=(5, 0))

        tk.Label(main_frame, text=f"Asunto (<= {MAX_SUBJECT} caracteres):",
                font=("Segoe UI", 10, "bold")).grid(row=2, column=0, sticky="w", pady=5)
        self.subject_var = tk.StringVar()
        self.subject_var.trace("w", self.update_counter)
        tk.Entry(main_frame, textvariable=self.subject_var, width=48,
                font=("Segoe UI", 10)).grid(row=2, column=1, columnspan=2, sticky="w", pady=5)
        self.counter_label = tk.Label(main_frame, text="0/72", fg="gray",
                                    font=("Segoe UI", 9))
        self.counter_label.grid(row=2, column=3, padx=5, pady=5)

        tk.Label(main_frame, text="Descripcion (opcional):",
                font=("Segoe UI", 10, "bold")).grid(row=3, column=0, sticky="nw", pady=5)
        self.body_text = scrolledtext.ScrolledText(main_frame, width=65, height=8,
                                                 font=("Segoe UI", 9))
        self.body_text.grid(row=3, column=1, columnspan=3, pady=5)

        tk.Label(main_frame, text="Footer (ej: Closes #123):",
                font=("Segoe UI", 10, "bold")).grid(row=4, column=0, sticky="w", pady=5)
        self.footer_var = tk.StringVar()
        tk.Entry(main_frame, textvariable=self.footer_var, width=50,
                font=("Segoe UI", 10)).grid(row=4, column=1, columnspan=2, sticky="w", pady=5)

        btn_frame = tk.Frame(main_frame)
        btn_frame.grid(row=5, column=0, columnspan=4, pady=15)

        self.pull_before_push = tk.BooleanVar(value=True)
        tk.Checkbutton(
            btn_frame,
            text="Hacer pull --rebase antes del push",
            variable=self.pull_before_push,
            font=("Segoe UI", 9)
        ).pack(side=tk.TOP, pady=(0, 8))
        self.exit_after_action = tk.BooleanVar(value=True)
        tk.Checkbutton(
            btn_frame,
            text="Salir automáticamente después de commit/push",
            variable=self.exit_after_action,
            font=("Segoe UI", 9)
        ).pack(side=tk.TOP, pady=(0, 8))

        button_row = tk.Frame(btn_frame)
        button_row.pack()

        tk.Button(button_row, text="COMMIT + PUSH", command=self.execute_full_flow,
                 bg="#0288d1", fg="white", font=("Segoe UI", 10, "bold"),
                 padx=15, pady=8, relief="flat", cursor="hand2", width=16).pack(side=tk.LEFT, padx=5)

        tk.Button(button_row, text="SOLO COMMIT", command=self.execute_commit_only,
                 bg="#388e3c", fg="white", font=("Segoe UI", 10, "bold"),
                 padx=15, pady=8, relief="flat", cursor="hand2", width=16).pack(side=tk.LEFT, padx=5)

        tk.Button(button_row, text="Refresh estado", command=self.refresh_git_status,
                 bg="#6c757d", fg="white", font=("Segoe UI", 9),
                 padx=12, pady=6, relief="flat").pack(side=tk.LEFT, padx=5)

        tk.Button(button_row, text="Cancelar", command=self.root.quit,
                 bg="#d32f2f", fg="white", font=("Segoe UI", 10),
                 padx=15, pady=6, relief="flat").pack(side=tk.RIGHT, padx=5)
             
    def on_scope_selected(self, event=None):
        selected = self.scope_var.get()
        if selected == "Personalizar...":
            self.enable_custom_scope()
            self.scope_var.set("")

    def enable_custom_scope(self):
        self.scope_combo.config(state="normal")
        self.scope_combo.focus()
        self.scope_combo.selection_range(0, tk.END)
        
    def suggest_scope_from_files(self):
        """Sugiere un ámbito basado en las rutas de los archivos staged."""
        if not self.staged_files:
            return "tasks"  # valor por defecto

        # Contador de scopes probables
        scope_votes = {scope: 0 for scope in PREDEFINED_SCOPES}
        
        for file_path in self.staged_files:
            path_lower = file_path.lower()
            for scope in PREDEFINED_SCOPES:
                if scope in path_lower:
                    scope_votes[scope] += 1

        # Elegir el scope con más votos
        best_scope = max(scope_votes, key=scope_votes.get)
        if scope_votes[best_scope] > 0:
            return best_scope
        else:
            return "tasks"  # fallback

    def update_counter(self, *args):
        count = len(self.subject_var.get())
        color = "red" if count > MAX_SUBJECT else "gray"
        self.counter_label.config(text=f"{count}/{MAX_SUBJECT}", fg=color)

    def execute_full_flow(self):
        if not self.staged_files:
            if not self.unstaged_files:
                messagebox.showwarning("Advertencia", "No hay cambios en el repositorio")
                return

            if not messagebox.askyesno("Sin cambios staged",
                "No hay archivos staged para commit.\n\n"
                "Stagear TODOS los cambios sin stagear y continuar?"):
                return

            self.stage_all_changes()
            if not self.staged_files:
                return

        if not self.type_var.get():
            self.status_var.set("Error: Selecciona un tipo de commit")
            return

        scope = self.scope_var.get().strip()
        if not scope:
            self.status_var.set("Error: El ambito no puede estar vacio")
            return

        if any(c in scope for c in "():"):
            self.status_var.set("Error: Ambito invalido: no puede contener '(', ')' o ':'")
            return

        subject = self.subject_var.get().strip()
        if not subject:
            self.status_var.set("Error: El asunto no puede estar vacio")
            return
        if len(subject) > MAX_SUBJECT:
            if not messagebox.askyesno("Advertencia",
                f"El asunto excede {MAX_SUBJECT} caracteres ({len(subject)}).\n"
                "Forzar commit? (no recomendado para estandares)"):
                return

        commit_type = self.type_var.get().split(" - ")[0]
        summary = f"{commit_type}({scope}): {subject[:50]}{'...' if len(subject)>50 else ''}"

        if not messagebox.askyesno("Confirmar operacion",
            f"Se ejecutara:\n"
            f"1. git commit -m \"{summary}\"\n"
            f"2. {'git pull --rebase origin ' + self.current_branch + ' (si esta marcado)' if self.pull_before_push.get() else ''}\n"
            f"3. git push origin {self.current_branch}\n\n"
            "Continuar?"):
            return

        try:
            if self.pull_before_push.get():
                self.status_var.set(f"Haciendo pull de origin/{self.current_branch}...")
                self.root.update()
                pull_result = subprocess.run(
                    ["git", "pull", "--rebase", "origin", self.current_branch],
                    capture_output=True, text=True, cwd=self.repo_path, timeout=20
                )
                if pull_result.returncode != 0:
                    self.handle_git_error(pull_result, "pull")
                    return

            body = self.body_text.get("1.0", tk.END).strip()
            footer = self.footer_var.get().strip()
            lines = [f"{commit_type}({scope}): {subject}", ""]
            if body: lines.extend([body, ""])
            if footer: lines.append(footer)
            commit_msg = "\n".join(lines)

            commit_result = subprocess.run(
                ["git", "commit", "-m", commit_msg],
                capture_output=True, text=True, cwd=self.repo_path, timeout=15
            )

            if commit_result.returncode != 0:
                self.handle_git_error(commit_result, "commit")
                return

            commit_hash = subprocess.run(
                ["git", "rev-parse", "--short", "HEAD"],
                capture_output=True, text=True, cwd=self.repo_path, timeout=5
            ).stdout.strip()

            self.status_var.set(f"Haciendo push a origin/{self.current_branch}...")
            self.root.update()

            push_result = subprocess.run(
                ["git", "push", "origin", self.current_branch],
                capture_output=True, text=True, cwd=self.repo_path, timeout=30
            )

            if push_result.returncode != 0:
                self.handle_git_error(push_result, "push")
                return

            messagebox.showinfo("Exito",
                f"Commit creado: {commit_hash}\n"
                f"Push completado a origin/{self.current_branch}\n\n"
                f"Mensaje: {summary}")
            if self.exit_after_action.get():
                self.root.quit()
            else:
                self.refresh_git_status()

        except subprocess.TimeoutExpired:
            op = "pull" if self.pull_before_push.get() and "pull" in locals() else "commit/push"
            messagebox.showerror("Timeout",
                f"Operacion {op} tardo demasiado.\n"
                "Causas posibles: hooks complejos, conexion lenta o muchos archivos.")
        except Exception as e:
            messagebox.showerror("Error inesperado",
                f"Tipo: {type(e).__name__}\nMensaje: {str(e)}")
                
    def execute_commit_only(self):
        """Ejecuta solo el commit, sin push."""
        if not self.staged_files:
            if not self.unstaged_files:
                messagebox.showwarning("Advertencia", "No hay cambios en el repositorio")
                return

            if not messagebox.askyesno("Sin cambios staged",
                "No hay archivos staged para commit.\n\n"
                "Stagear TODOS los cambios sin stagear y continuar?"):
                return

            self.stage_all_changes()
            if not self.staged_files:
                return

        if not self.type_var.get():
            self.status_var.set("Error: Selecciona un tipo de commit")
            return

        scope = self.scope_var.get().strip()
        if not scope:
            self.status_var.set("Error: El ambito no puede estar vacio")
            return

        if any(c in scope for c in "():"):
            self.status_var.set("Error: Ambito invalido: no puede contener '(', ')' o ':'")
            return

        subject = self.subject_var.get().strip()
        if not subject:
            self.status_var.set("Error: El asunto no puede estar vacio")
            return
        if len(subject) > MAX_SUBJECT:
            if not messagebox.askyesno("Advertencia",
                f"El asunto excede {MAX_SUBJECT} caracteres ({len(subject)}).\n"
                "Forzar commit? (no recomendado para estandares)"):
                return

        commit_type = self.type_var.get().split(" - ")[0]
        summary = f"{commit_type}({scope}): {subject[:50]}{'...' if len(subject)>50 else ''}"

        if not messagebox.askyesno("Confirmar commit local",
            f"Se ejecutara:\n"
            f"git commit -m \"{summary}\"\n\n"
            "Continuar?"):
            return

        try:
            body = self.body_text.get("1.0", tk.END).strip()
            footer = self.footer_var.get().strip()
            lines = [f"{commit_type}({scope}): {subject}", ""]
            if body: lines.extend([body, ""])
            if footer: lines.append(footer)
            commit_msg = "\n".join(lines)

            commit_result = subprocess.run(
                ["git", "commit", "-m", commit_msg],
                capture_output=True, text=True, cwd=self.repo_path, timeout=15, encoding="utf-8"
            )

            if commit_result.returncode != 0:
                self.handle_git_error(commit_result, "commit")
                return

            commit_hash = subprocess.run(
                ["git", "rev-parse", "--short", "HEAD"],
                capture_output=True, text=True, cwd=self.repo_path, timeout=5, encoding="utf-8"
            ).stdout.strip()

            messagebox.showinfo("Exito",
                f"Commit creado localmente: {commit_hash}\n\n"
                f"Mensaje: {summary}")
            if self.exit_after_action.get():
                self.root.quit()
            else:
                self.refresh_git_status()

        except subprocess.TimeoutExpired:
            messagebox.showerror("Timeout", "El commit tardo demasiado.")
        except Exception as e:
            messagebox.showerror("Error inesperado",
                f"Tipo: {type(e).__name__}\nMensaje: {str(e)}")
    def handle_git_error(self, result, operation):
        stderr = result.stderr.strip()
        stdout = result.stdout.strip()

        if "husky" in stderr.lower() or "pre-commit" in stderr.lower():
            hint = "Hooks de pre-commit fallidos. Ejecuta manualmente:\ngit commit --no-verify"
        elif "rejected" in stderr.lower() and "non-fast-forward" in stderr.lower():
            hint = f"Tu rama esta desactualizada.\nPrimero haz: git pull --rebase origin {self.current_branch}"
        elif "authentication" in stderr.lower() or "password" in stderr.lower() or "ssh" in stderr.lower():
            hint = "Error de autenticacion. Verifica:\n- SSH key configurada\n- Token de acceso para HTTPS"
        elif "nothing to commit" in stdout.lower():
            hint = "No hay cambios para commitear (ya commiteaste?)"
        else:
            hint = f"stderr:\n{stderr[:400]}"

        messagebox.showerror(f"Error en {operation}",
            f"Codigo: {result.returncode}\n\n{hint}")
        self.status_var.set(f"Fallo {operation} - revisa mensajes anteriores")


if __name__ == "__main__":
    try:
        import tkinter
        root = tk.Tk()
        root.withdraw()
        root.update()
        root.destroy()

        root = tk.Tk()
        app = GitCommitHelper(root)
        root.mainloop()

    except ModuleNotFoundError as e:
        if "tkinter" in str(e).lower():
            print("ERROR: Tkinter no esta instalado")
            print("\nSolucion segun tu sistema:")
            print("  - Ubuntu/Debian: sudo apt install python3-tk")
            print("  - macOS: brew install python-tk")
            print("  - Windows: Reinstala Python y marca 'tcl/tk and IDLE' en el instalador")
            print("\nAlternativa rapida (terminal):")
            print("  git add .")
            print('  git commit -m "feat(mi-ambito-personalizado): ejemplo"')
            print("  git push")
        else:
            raise
    except Exception as e:
        print(f"Error critico: {type(e).__name__}: {str(e)}")
        sys.exit(1)