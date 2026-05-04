package com.normocontrol.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Slf4j
@Service
public class GitService {

    public File cloneRepository(String url, String branch) throws GitAPIException, IOException {
        String fullUrl = url;
        // Handle short GitHub URLs
        if (!url.startsWith("http") && !url.startsWith("git@") && url.contains("/")) {
            fullUrl = "https://github.com/" + url + ".git";
        } else if (url.startsWith("https://github.com/") && !url.endsWith(".git")) {
            fullUrl = url + ".git";
        }

        Path tempDir = Files.createTempDirectory("normo_repo_");
        log.info("Cloning repository {} to {}", fullUrl, tempDir);

        Git git = null;
        try {
            // Step 1: Clone the default branch (always works if repo exists)
            git = Git.cloneRepository()
                    .setURI(fullUrl)
                    .setDirectory(tempDir.toFile())
                    .call();
            
            // Step 2: If user specified a specific branch, try to switch to it
            if (branch != null && !branch.trim().isEmpty() && !branch.equals("main")) {
                try {
                    log.info("Attempting to checkout branch: {}", branch);
                    git.checkout()
                            .setName(branch)
                            .call();
                } catch (GitAPIException e) {
                    log.warn("Could not checkout branch '{}', staying on default branch. Error: {}", branch, e.getMessage());
                }
            } else if ("main".equals(branch)) {
                // If branch is 'main' but we are already on 'master', we are fine.
                // We only try to switch if we are NOT on main already.
                String currentBranch = git.getRepository().getBranch();
                if (!"main".equals(currentBranch)) {
                    try {
                        git.checkout().setName("main").call();
                    } catch (GitAPIException e) {
                        log.info("Branch 'main' not found, staying on default branch '{}'", currentBranch);
                    }
                }
            }

            return tempDir.toFile();
        } catch (GitAPIException e) {
            deleteDirectory(tempDir.toFile());
            throw e;
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }

    public void deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) return;
        try {
            Files.walk(directory.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            log.error("Failed to delete directory: {}", directory.getAbsolutePath(), e);
        }
    }
}
