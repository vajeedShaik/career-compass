import { useCallback, useState } from "react";
import { useDropzone, type FileRejection } from "react-dropzone";
import { motion, AnimatePresence } from "framer-motion";
import { UploadCloud, FileText, X, CheckCircle2 } from "lucide-react";
import { cn } from "@/utils/cn";
import { Button } from "@/components/ui/button";

interface DropzoneProps {
  onFileSelected: (file: File) => void;
  disabled?: boolean;
}

const MAX_SIZE_BYTES = 10 * 1024 * 1024; // matches backend's 10MB multipart limit

export function Dropzone({ onFileSelected, disabled }: DropzoneProps) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const onDrop = useCallback(
    (acceptedFiles: File[], rejections: FileRejection[]) => {
      setError(null);
      if (rejections.length > 0) {
        setError(rejections[0].errors[0]?.message ?? "File was rejected");
        return;
      }
      const file = acceptedFiles[0];
      if (!file) return;

      setSelectedFile(file);
      setPreviewUrl(URL.createObjectURL(file));
      onFileSelected(file);
    },
    [onFileSelected]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: { "application/pdf": [".pdf"] },
    maxSize: MAX_SIZE_BYTES,
    maxFiles: 1,
    disabled,
  });

  function clearFile() {
    setSelectedFile(null);
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setPreviewUrl(null);
    setError(null);
  }

  if (selectedFile) {
    return (
      <div className="rounded-lg border border-border bg-card p-4">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-success/10">
            <CheckCircle2 className="h-5 w-5 text-success" />
          </div>
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium">{selectedFile.name}</p>
            <p className="text-xs text-muted-foreground">
              {(selectedFile.size / 1024).toFixed(0)} KB
            </p>
          </div>
          <Button variant="ghost" size="icon" onClick={clearFile} disabled={disabled}>
            <X className="h-4 w-4" />
          </Button>
        </div>
        {previewUrl && (
          <iframe
            src={previewUrl}
            title="PDF preview"
            className="mt-4 h-64 w-full rounded-md border border-border"
          />
        )}
      </div>
    );
  }

  return (
    <div>
      <div
        {...getRootProps()}
        className={cn(
          "flex cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed border-border bg-muted/20 px-6 py-10 text-center transition-colors",
          isDragActive && "border-primary bg-primary/5",
          disabled && "cursor-not-allowed opacity-50"
        )}
      >
        <input {...getInputProps()} />
        <AnimatePresence mode="wait">
          <motion.div
            key={isDragActive ? "active" : "idle"}
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.9, opacity: 0 }}
            transition={{ duration: 0.15 }}
          >
            {isDragActive ? (
              <UploadCloud className="mx-auto h-10 w-10 text-primary" />
            ) : (
              <FileText className="mx-auto h-10 w-10 text-muted-foreground" />
            )}
          </motion.div>
        </AnimatePresence>
        <p className="mt-3 text-sm font-medium">
          {isDragActive ? "Drop your resume here" : "Drag and drop your resume PDF"}
        </p>
        <p className="mt-1 text-xs text-muted-foreground">or click to browse — PDF, up to 10MB</p>
      </div>
      {error && <p className="mt-2 text-xs text-destructive">{error}</p>}
    </div>
  );
}
